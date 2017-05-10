package com.bankbitcoinow.bitcoinj;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.PeerGroup;
import org.bitcoinj.core.TransactionBroadcast;
import org.bitcoinj.signers.TransactionSigner;
import org.bitcoinj.wallet.SendRequest;
import org.bitcoinj.wallet.Wallet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class BitcoinjFacade {

	private static final Logger LOG = LoggerFactory.getLogger(BitcoinjFacade.class);

	private final NetworkParameters networkParams;
	private final Wallet wallet;
	private final PeerGroup peerGroup;
	private final SingleKeyTransactionSigner transactionSigner;
	private SecureRandom secureRandom = null;

	@Autowired
	public BitcoinjFacade(NetworkParameters networkParams,
	                      Wallet wallet,
	                      PeerGroup peerGroup) {
		this.networkParams = networkParams;
		this.wallet = wallet;
		this.peerGroup = peerGroup;
		this.transactionSigner = findTransactionSigner(wallet);
	}

	private SingleKeyTransactionSigner findTransactionSigner(Wallet wallet) {
		for (TransactionSigner transactionSigner : wallet.getTransactionSigners()) {
			if (transactionSigner instanceof SingleKeyTransactionSigner) {
				return (SingleKeyTransactionSigner) transactionSigner;
			}
		}

		throw new IllegalArgumentException("Wallet does not contain SingleKeyTransactionSigner");
	}

	/**
	 * Generates new key, adds public part to the wallet
	 * and return encrypted using given password.
	 *
	 * @param password Password used to encrypt key.
	 * @return Encrypted key with all information (except password) required to decrypt it.
	 */
	public EncryptedKey generateNewKey(String password) {
		ECKey newKey = secureRandom != null ? new ECKey(secureRandom) : new ECKey();
		ECKey publicKey = ECKey.fromPublicOnly(newKey.getPubKeyPoint());
		Address address = newKey.toAddress(networkParams);

		if (LOG.isDebugEnabled()) {
			LOG.info("Successfully generated new key: {}", newKey.toStringWithPrivate(networkParams));
		} else {
			LOG.info("Successfully generated new key: {}", newKey.toString());
		}

		LOG.info("Address for new key: {}", address);

		wallet.addWatchedAddress(address);
		wallet.importKey(publicKey);

		return EncryptedKey.encryptKey(newKey, password);
	}

	public Address getAddress(String base58) {
		return Address.fromBase58(networkParams, base58);
	}

	public SendRequestBuilder prepareSendRequest() {
		return new SendRequestBuilder(wallet);
	}

	/**
	 * Signs prepared SendRequest using given key
	 * and broadcasts final transaction to the network.
	 *
	 * @param sendRequest SendRequest prepared using {@link #prepareSendRequest()}.
	 * @param encryptedKey Key used to sign transaction.
	 *                     Have to match address specified in {@link SendRequestBuilder#from(Address)}.
	 * @param password     Password to decrypt key.
	 * @return Information about (potentially in progress) broadcast.
	 *         After successful broadcasting result can be read from {@link TransactionBroadcast#future()}.
	 */
	public synchronized TransactionBroadcast signAndBroadcastSendRequest(SendRequest sendRequest,
	                                                                     EncryptedKey encryptedKey,
	                                                                     String password) {
		signSendRequest(sendRequest, encryptedKey, password);
		return broadcastTransaction(sendRequest);
	}

	public synchronized void signSendRequest(SendRequest sendRequest, EncryptedKey encryptedKey, String password) {
		try {
			ECKey key = encryptedKey.decryptKey(password);
			transactionSigner.setKey(key, null);
			wallet.signTransaction(sendRequest);
		} finally {
			transactionSigner.clearKey();
		}
	}

	public TransactionBroadcast broadcastTransaction(SendRequest sendRequest) {
		return peerGroup.broadcastTransaction(sendRequest.tx);
	}

	void setSecureRandom(SecureRandom secureRandom) {
		this.secureRandom = secureRandom;
	}
}
