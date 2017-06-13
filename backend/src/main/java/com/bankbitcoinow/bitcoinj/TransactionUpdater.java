package com.bankbitcoinow.bitcoinj;

import com.bankbitcoinow.models.TransactionStatus;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionConfidence;
import org.bitcoinj.core.TransactionConfidence.ConfidenceType;
import org.bitcoinj.core.TransactionInput;
import org.bitcoinj.core.TransactionOutput;
import org.bitcoinj.core.listeners.TransactionConfidenceEventListener;
import org.bitcoinj.script.Script;
import org.bitcoinj.wallet.RedeemData;
import org.bitcoinj.wallet.Wallet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import com.bankbitcoinow.services.AddressService;
import com.bankbitcoinow.services.TransactionService;

import java.sql.Timestamp;

@Component
@Order(BlockChainDownloader.PRECEDENCE - 1)
public class TransactionUpdater implements CommandLineRunner, TransactionConfidenceEventListener {

	private static final Logger LOG = LoggerFactory.getLogger(TransactionUpdater.class);

	private final Wallet wallet;
	private final AddressService addressService;
	private final TransactionService transactionService;

	@Autowired
	public TransactionUpdater(Wallet wallet,
	                          AddressService addressService,
	                          TransactionService transactionService) {
		this.wallet = wallet;
		this.addressService = addressService;
		this.transactionService = transactionService;
	}

	@Override
	public void run(String... args) throws Exception {
		LOG.info("Preparing TransactionUpdater...");
		registerWalletListeners();
	}

	private void registerWalletListeners() {
		LOG.info("Registering wallet listeners...");
		wallet.addTransactionConfidenceEventListener(this);
		LOG.info("Wallet listeners registered");
	}

	@Override
	public void onTransactionConfidenceChanged(Wallet wallet, Transaction tx) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("Confidence changed for transaction: {}", tx);
		}

		for (TransactionOutput txOutput : tx.getOutputs()) {
			try {
				processOutput(txOutput);
			} catch (Exception e) {
				LOG.error("Error while processing output: {}", txOutput, e);
			}
		}

		for (TransactionInput txInput : tx.getInputs()) {
			try {
				processInput(txInput);
			} catch (Exception e) {
				LOG.error("Error while processing input: {}", txInput, e);
			}
		}
	}

	private void processOutput(TransactionOutput txOutput) {
		LOG.debug("Checking output: {}", txOutput);

		Script script = txOutput.getScriptPubKey();
		if (!script.isSentToAddress()) {
			LOG.debug("Only outputs sent to address are supported - skipping");
			return;
		}

		if (!txOutput.isMineOrWatched(wallet)) {
			LOG.debug("Output is not related to any key in wallet - skipping");
			return;
		}

		Address btcAddress = script.getToAddress(txOutput.getParams());
		processTransaction(txOutput.getParentTransaction(), txOutput.getValue(), true, btcAddress);
	}

	private void processInput(TransactionInput txInput) {
		LOG.debug("Checking input: {}", txInput);

		RedeemData redeemData;
		try {
			redeemData = txInput.getConnectedRedeemData(wallet);
		} catch (Exception e) {
			if (e.getMessage().equals("Input is not connected so cannot retrieve key")) {
				redeemData = null;
			} else {
				throw e;
			}
		}

		if (redeemData == null) {
			LOG.debug("No Redeem data found - skipping");
			return;
		}

		// For pay-to-address and pay-to-key inputs RedeemData will always contain only one key
		// We only support pay-to-address
		if (redeemData.keys.size() != 1) {
			LOG.debug("Only inputs sent from address are supported - skipping");
			return;
		}

		ECKey pubKey = redeemData.keys.get(0);
		Address btcAddress = pubKey.toAddress(txInput.getParams());
		processTransaction(txInput.getParentTransaction(), txInput.getValue().negate(), false, btcAddress);
	}

	private void processTransaction(Transaction tx, Coin value, boolean incoming, Address btcAddress) {
		String destinationAddress = btcAddress.toBase58();
		String txHash = tx.getHashAsString();
		com.bankbitcoinow.models.Transaction transaction = transactionService.find(txHash, destinationAddress);

		if (transaction != null) {
			updateExistingTransaction(tx, transaction);
		} else {
			addNewTransaction(tx, value, incoming, destinationAddress);
		}
	}

	void updateExistingTransaction(Transaction tx, com.bankbitcoinow.models.Transaction transaction) {
		LOG.debug("Found existing transaction in database");

		TransactionConfidence confidence = tx.getConfidence();
		TransactionStatus currentStatus = transaction.getStatus();
		int currentConfirmations = transaction.getConfirmations();
		updateStatus(transaction, confidence);
		updateConfirmations(transaction, confidence);

		boolean statusChanged = transaction.getStatus() != currentStatus;
		boolean confirmationsChanged = transaction.getConfirmations() != currentConfirmations;
		if (statusChanged || confirmationsChanged) {
			transactionService.updateTransction(transaction);
		}

		if (statusChanged && transaction.getStatus() == TransactionStatus.CONFIRMED) {
			com.bankbitcoinow.models.Address address = transaction.getAddress();

			if (transaction.isIncoming()) {
				address.addAmount(transaction.getAmount());
			} else {
				address.takeAmount(transaction.getAmount());
			}

			addressService.updateAddress(address);
		}
	}

	void addNewTransaction(Transaction tx, Coin value, boolean incoming, String addressStr) {
		com.bankbitcoinow.models.Address dbAddress = addressService.findByAddress(addressStr);

		if (dbAddress == null) {
			LOG.warn("Output or input is from our wallet, but there is address {} in the database", addressStr);
			return;
		}

		LOG.info("Creating new transaction in database");

		TransactionConfidence confidence = tx.getConfidence();

		com.bankbitcoinow.models.Transaction transaction = new com.bankbitcoinow.models.Transaction();
		transaction.setHash(tx.getHashAsString());
		transaction.setAddress(dbAddress);

		if (incoming) {
			transaction.setDestinationAddress(addressStr);
		} else {
			transaction.setSourceAddress(addressStr);
		}

		transaction.setAmount(value);
		transaction.setCreatedAt(new Timestamp(System.currentTimeMillis()));
		transaction.setStatus(getNewStatus(TransactionStatus.UNCONFIRMED, confidence));
		updateConfirmations(transaction, confidence);
		transaction.setBlockchainData(tx.unsafeBitcoinSerialize());
		transactionService.addTransaction(transaction);
	}

	private void updateStatus(com.bankbitcoinow.models.Transaction transaction,
	                          TransactionConfidence confidence) {
		TransactionStatus currentStatus = transaction.getStatus();
		TransactionStatus newStatus = getNewStatus(currentStatus, confidence);

		if (newStatus != currentStatus) {
			LOG.info("Changing status of transaction {} from {} to {}",
					transaction.getId(), currentStatus, newStatus);
			transaction.setStatus(newStatus);
		}
	}

	static TransactionStatus getNewStatus(TransactionStatus currentStatus,
	                                              TransactionConfidence confidence) {
		switch (confidence.getConfidenceType()) {
			case PENDING:
				return TransactionStatus.UNCONFIRMED;
			case BUILDING:
				return TransactionStatus.CONFIRMED;
			default:
				return currentStatus;
		}
	}

	void updateConfirmations(com.bankbitcoinow.models.Transaction transaction,
	                                 TransactionConfidence confidence) {
		if (transaction.getStatus() == TransactionStatus.CONFIRMED
				&& confidence.getConfidenceType() == ConfidenceType.BUILDING) {
			int lastBlockSeenHeight = wallet.getLastBlockSeenHeight();
			int appearedAtChainHeight = confidence.getAppearedAtChainHeight();
			int currentConfirmations = transaction.getConfirmations();
			int newConfirmations = lastBlockSeenHeight - appearedAtChainHeight;

			if (newConfirmations != currentConfirmations) {
				LOG.info("Changing number of confirmations of transaction {} from {} to {}",
						transaction.getId(), currentConfirmations, newConfirmations);
				transaction.setConfirmations(newConfirmations);
			}
		}
	}
}
