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
import org.spongycastle.crypto.params.KeyParameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BitcoinjFacade {

	private static final Logger LOG = LoggerFactory.getLogger(BitcoinjFacade.class);

	private final NetworkParameters networkParams;
	private final Wallet wallet;
	private final PeerGroup peerGroup;
	private final SingleKeyTransactionSigner transactionSigner;

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
	 * @param key         Key used to sign transaction.
	 *                    Have to match address specified in {@link SendRequestBuilder#from(Address)}.
	 * @param aesKey      Optional AES key if {@code key} is encrypted.
	 * @return Information about (potentially in progress) broadcast.
	 *         After successful broadcasting result can be read from {@link TransactionBroadcast#future()}.
	 */
	public synchronized TransactionBroadcast signAndBroadcastSendRequest(SendRequest sendRequest,
	                                                                     ECKey key,
	                                                                     KeyParameter aesKey) {
		try {
			transactionSigner.setKey(key, aesKey);
			wallet.signTransaction(sendRequest);
		} finally {
			transactionSigner.clearKey();
		}

		return peerGroup.broadcastTransaction(sendRequest.tx);
	}
}
