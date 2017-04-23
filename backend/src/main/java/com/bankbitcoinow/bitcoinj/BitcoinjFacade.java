package com.bankbitcoinow.bitcoinj;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.PeerGroup;
import org.bitcoinj.core.TransactionBroadcast;
import org.bitcoinj.wallet.SendRequest;
import org.bitcoinj.wallet.Wallet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BitcoinjFacade {

	private static final Logger LOG = LoggerFactory.getLogger(BitcoinjFacade.class);

	private final NetworkParameters networkParams;
	private final Wallet wallet;
	private final PeerGroup peerGroup;

	public BitcoinjFacade(NetworkParameters networkParams, Wallet wallet, PeerGroup peerGroup) {
		this.networkParams = networkParams;
		this.wallet = wallet;
		this.peerGroup = peerGroup;
	}

	public Address getAddress(String base58) {
		return Address.fromBase58(networkParams, base58);
	}

	public SendRequestBuilder prepareSendRequest() {
		return new SendRequestBuilder(wallet);
	}

	public TransactionBroadcast signAndBroadcastSendRequest(SendRequest sendRequest) {
		wallet.signTransaction(sendRequest);
		return peerGroup.broadcastTransaction(sendRequest.tx);
	}
}
