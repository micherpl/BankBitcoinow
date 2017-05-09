package com.bankbitcoinow.bitcoinj;

import com.bankbitcoinow.models.TransactionStatus;
import com.bankbitcoinow.models.User;
import com.bankbitcoinow.services.UserService;
import org.bitcoinj.core.AbstractBlockChain;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.StoredBlock;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionConfidence;
import org.bitcoinj.core.TransactionConfidence.ConfidenceType;
import org.bitcoinj.core.TransactionOutput;
import org.bitcoinj.core.Utils;
import org.bitcoinj.core.VerificationException;
import org.bitcoinj.core.listeners.TransactionConfidenceEventListener;
import org.bitcoinj.core.listeners.TransactionReceivedInBlockListener;
import org.bitcoinj.script.Script;
import org.bitcoinj.wallet.Wallet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import com.bankbitcoinow.services.AddressService;
import com.bankbitcoinow.services.TransactionService;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.sql.Timestamp;

@Component
@Order(BlockChainDownloader.PRECEDENCE - 1)
public class TransactionUpdater implements CommandLineRunner, TransactionConfidenceEventListener {

	private static final Logger LOG = LoggerFactory.getLogger(TransactionUpdater.class);

	private final Wallet wallet;
	private final AbstractBlockChain blockChain;
	private final BitcoinjFacade bitcoinjFacade;
	private final AddressService addressService;
	private final TransactionService transactionService;
	private final UserService userService;

	@Autowired
	public TransactionUpdater(Wallet wallet,
	                          AbstractBlockChain blockChain,
	                          BitcoinjFacade bitcoinjFacade,
	                          AddressService addressService,
	                          TransactionService transactionService,
	                          UserService userService) {
		this.wallet = wallet;
		this.blockChain = blockChain;
		this.bitcoinjFacade = bitcoinjFacade;
		this.addressService = addressService;
		this.transactionService = transactionService;
		this.userService = userService;
	}

	@Override
	public void run(String... args) throws Exception {
		LOG.info("Preparing TransactionUpdater...");
		registerWalletListeners();

		SecureRandom secureRandom = new SecureRandom();
		secureRandom.setSeed(123);
		bitcoinjFacade.setSecureRandom(secureRandom);

		User user = userService.findByEmail("koziol.pawel@gmail.com");
		if (user != null) {
			LOG.info("Found user {} in database. ID: {}", user.getEmail(), user.getId());
		} else {
			LOG.info("Creating new user...");
			user = new User();
			user.setEmail("koziol.pawel@gmail.com");
			user.setPassword("!QAZ2wsx");
			user.setCreatedAt(new Timestamp(System.currentTimeMillis()));
			user = userService.save(user);
			LOG.info("User created. ID: {}", user.getId());
		}

		EncryptedKey encryptedKey = bitcoinjFacade.generateNewKey("abc");
		com.bankbitcoinow.models.Address address = addressService.findByAddress(encryptedKey.getAddress(wallet.getParams()));

		if (address != null) {
			LOG.info("Found address {} in database. ID: {}", address.getAddress(), address.getId());
		} else {
			LOG.info("Creating new address...");
			address = new com.bankbitcoinow.models.Address();
			address.setUser(user);
			address.setAddress(encryptedKey.getAddress(wallet.getParams()));
			address.setPrivateKey(encryptedKey.toByteArray());
			address.setBalance(new BigDecimal(0));
			address.setCreated_at(new Timestamp(System.currentTimeMillis()));
			address = addressService.addAddress(address);
			LOG.info("Address created. ID: {}", address.getId());
		}

		LOG.info("Key: {}", Utils.HEX.encode(encryptedKey.toByteArray()));
	}

	private void registerWalletListeners() {
		LOG.info("Registering wallet listeners...");

		wallet.addChangeEventListener(w -> LOG.info("Wallet changed"));
		wallet.addCoinsReceivedEventListener((w, tx, prevBalance, newBalance) -> LOG.info("Coins received in transaction {}. Prev: {}. New: {}", tx, prevBalance, newBalance));
		wallet.addCoinsSentEventListener((w, tx, prevBalance, newBalance) -> LOG.info("Coins sent in transaction {}. Prev: {}. New: {}", tx, prevBalance, newBalance));
		wallet.addTransactionConfidenceEventListener((w, tx) -> LOG.info("Transaction confidence chanced for: {}. Value: {}", tx, tx.getConfidence()));
		wallet.addTransactionConfidenceEventListener(this);

		blockChain.addTransactionReceivedListener(new TransactionReceivedInBlockListener() {
			@Override
			public void receiveFromBlock(Transaction tx, StoredBlock block, AbstractBlockChain.NewBlockType blockType, int relativityOffset) throws VerificationException {
				LOG.info("Received transaction from block: {}", tx);
				LOG.info("Relative offset: {}", relativityOffset);
				LOG.info("Wallet: {}", wallet);
			}

			@Override
			public boolean notifyTransactionIsInBlock(Sha256Hash txHash, StoredBlock block, AbstractBlockChain.NewBlockType blockType, int relativityOffset) throws VerificationException {
				return false;
			}
		});
	}

	@Override
	public void onTransactionConfidenceChanged(Wallet wallet, Transaction tx) {
		LOG.info("Confidence changed for transaction: {}", tx);

		for (TransactionOutput txOutput : tx.getOutputs()) {
			LOG.info("Checking output: {}", txOutput);

			Script script = txOutput.getScriptPubKey();
			if (!script.isSentToAddress()) {
				LOG.info("Only outputs sent to address are supported - skipping");
				continue;
			}

			if (!txOutput.isMineOrWatched(wallet)) {
				LOG.info("Output is not related to any key in wallet - skipping");
				continue;
			}

			Address btcAddress = script.getToAddress(txOutput.getParams());
			String destinationAddress = btcAddress.toBase58();
			String txHash = tx.getHashAsString();
			com.bankbitcoinow.models.Transaction transaction = transactionService.find(txHash, destinationAddress);

			if (transaction != null) {
				updateExistingTransaction(tx, transaction);
			} else {
				addNewTransaction(tx, txOutput);
			}
		}
	}

	private void updateExistingTransaction(Transaction tx, com.bankbitcoinow.models.Transaction transaction) {
		LOG.info("Found existing transaction in database");

		TransactionConfidence confidence = tx.getConfidence();
		TransactionStatus currentStatus = transaction.getStatus();
		TransactionStatus newStatus = getNewStatus(currentStatus, confidence);

		if (newStatus != currentStatus) {
			LOG.info("Changing status of transaction {} from {} to {}",
					transaction.getId(), currentStatus, newStatus);
			transaction.setStatus(newStatus);

			updateConfirmations(transaction, confidence);

			transactionService.updateTransction(transaction);
		}
	}

	private void addNewTransaction(Transaction tx, TransactionOutput txOutput) {
		Script script = txOutput.getScriptPubKey();
		Address btcAddress = script.getToAddress(txOutput.getParams());
		String addressStr = btcAddress.toBase58();
		com.bankbitcoinow.models.Address dbAddress = addressService.findByAddress(addressStr);

		if (dbAddress == null) {
			LOG.warn("Output is from our wallet, but there is address {} in the database", addressStr);
			return;
		}

		LOG.info("Creating new transaction in database");

		TransactionConfidence confidence = tx.getConfidence();

		com.bankbitcoinow.models.Transaction transaction = new com.bankbitcoinow.models.Transaction();
		transaction.setHash(tx.getHashAsString());
		transaction.setAddress(dbAddress);
		transaction.setDestinationAddress(addressStr);
		transaction.setAmount(txOutput.getValue());
		transaction.setCreatedAt(new Timestamp(System.currentTimeMillis()));
		transaction.setStatus(getNewStatus(TransactionStatus.UNCONFIRMED, confidence));
		updateConfirmations(transaction, confidence);
		transaction.setBlockchainData(tx.unsafeBitcoinSerialize());
		transactionService.addTransaction(transaction);
	}

	private static TransactionStatus getNewStatus(TransactionStatus currentStatus,
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

	private void updateConfirmations(com.bankbitcoinow.models.Transaction transaction,
	                                 TransactionConfidence confidence) {
		if (transaction.getStatus() == TransactionStatus.CONFIRMED
				&& confidence.getConfidenceType() == ConfidenceType.BUILDING) {
			int lastBlockSeenHeight = wallet.getLastBlockSeenHeight();
			int appearedAtChainHeight = confidence.getAppearedAtChainHeight();
			transaction.setConfirmations(lastBlockSeenHeight - appearedAtChainHeight);
		}
	}
}
