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

import java.io.IOException;
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

		User user = getOrCreateUser("koziol.pawel@gmail.com", "!QAZ2wsx");
		getOrCreateAddress(user, generateKeyForSeed(123, "!QAZ2wsx"));
		getOrCreateAddress(user, generateKeyForSeed(123456, "!QAZ2wsx"));
		getOrCreateAddress(user, generateKeyForSeed(654321, "!QAZ2wsx"));
	}

	private User getOrCreateUser(String email, String password) {
		User user = userService.findByEmail(email);

		if (user != null) {
			LOG.info("Found user {} in database. ID: {}", user.getEmail(), user.getId());
		} else {
			LOG.info("Creating new user...");
			user = new User();
			user.setEmail(email);
			user.setPassword(password);
			user.setCreatedAt(new Timestamp(System.currentTimeMillis()));
			user = userService.save(user);
			LOG.info("User created. ID: {}", user.getId());
		}

		return user;
	}

	private EncryptedKey generateKeyForSeed(int seed, String password) {
		try {
			SecureRandom secureRandom = new SecureRandom();
			secureRandom.setSeed(seed);
			bitcoinjFacade.setSecureRandom(secureRandom);
			return bitcoinjFacade.generateNewKey(password);
		} finally {
			bitcoinjFacade.setSecureRandom(null);
		}
	}

	private com.bankbitcoinow.models.Address getOrCreateAddress(User user, EncryptedKey encryptedKey) throws IOException {
		String addressStr = encryptedKey.getAddress(wallet.getParams());
		com.bankbitcoinow.models.Address address = addressService.findByAddress(addressStr);

		if (address != null) {
			LOG.info("Found address {} in database. ID: {}", addressStr, address.getId());
		} else {
			LOG.info("Creating new address...");
			address = new com.bankbitcoinow.models.Address();
			address.setUser(user);
			address.setAddress(addressStr);
			address.setPrivateKey(encryptedKey.toByteArray());
			address.setBalance(new BigDecimal(0));
			address.setCreated_at(new Timestamp(System.currentTimeMillis()));
			address = addressService.addAddress(address);
			LOG.info("Address created. ID: {}", address.getId());
		}

		return address;
	}

	private void registerWalletListeners() {
		LOG.info("Registering wallet listeners...");

		wallet.addChangeEventListener(w -> LOG.debug("Wallet changed"));
		wallet.addCoinsReceivedEventListener((w, tx, prevBalance, newBalance) -> LOG.debug("Coins received in transaction {}. Prev: {}. New: {}", tx, prevBalance, newBalance));
		wallet.addCoinsSentEventListener((w, tx, prevBalance, newBalance) -> LOG.debug("Coins sent in transaction {}. Prev: {}. New: {}", tx, prevBalance, newBalance));
		wallet.addTransactionConfidenceEventListener((w, tx) -> LOG.debug("Transaction confidence chanced for: {}. Value: {}", tx, tx.getConfidence()));
		wallet.addTransactionConfidenceEventListener(this);

		blockChain.addTransactionReceivedListener(new TransactionReceivedInBlockListener() {
			@Override
			public void receiveFromBlock(Transaction tx, StoredBlock block, AbstractBlockChain.NewBlockType blockType, int relativityOffset) throws VerificationException {
				LOG.debug("Received transaction from block: {}", tx);
				LOG.debug("Relative offset: {}", relativityOffset);
				LOG.debug("Wallet: {}", wallet);
			}

			@Override
			public boolean notifyTransactionIsInBlock(Sha256Hash txHash, StoredBlock block, AbstractBlockChain.NewBlockType blockType, int relativityOffset) throws VerificationException {
				return false;
			}
		});
	}

	@Override
	public void onTransactionConfidenceChanged(Wallet wallet, Transaction tx) {
		if (LOG.isDebugEnabled()) {
			LOG.debug("Confidence changed for transaction: {}", tx);
		}

		for (TransactionOutput txOutput : tx.getOutputs()) {
			LOG.debug("Checking output: {}", txOutput);

			Script script = txOutput.getScriptPubKey();
			if (!script.isSentToAddress()) {
				LOG.debug("Only outputs sent to address are supported - skipping");
				continue;
			}

			if (!txOutput.isMineOrWatched(wallet)) {
				LOG.debug("Output is not related to any key in wallet - skipping");
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
		LOG.debug("Found existing transaction in database");

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
