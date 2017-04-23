package com.bankbitcoinow.bitcoinj;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.InsufficientMoneyException;
import org.bitcoinj.wallet.SendRequest;
import org.bitcoinj.wallet.Wallet;
import org.springframework.util.Assert;

public class SendRequestBuilder {

	private final Wallet wallet;
	private Address sourceAddress;
	private Address destinationAddress;
	private Address changeAddress;
	private Coin amount;
	private boolean sendAllAvailable = false;

	SendRequestBuilder(Wallet wallet) {
		this.wallet = wallet;
	}

	public SendRequestBuilder from(Address sourceAddress) {
		this.sourceAddress = sourceAddress;
		return this;
	}

	public SendRequestBuilder to(Address destinationAddress) {
		this.destinationAddress = destinationAddress;
		return this;
	}

	public SendRequestBuilder change(Address changeAddress) {
		this.changeAddress = changeAddress;
		return this;
	}

	public SendRequestBuilder amount(Coin amount) {
		Assert.state(!sendAllAvailable, "Amount cannot be set when sending all available cash");
		this.amount = amount;
		return this;
	}

	public SendRequestBuilder allAvailable() {
		Assert.state(amount == null, "Cannot sent available cash when amount was already set");
		this.sendAllAvailable = true;
		return this;
	}

	public SendRequest prepare() throws InsufficientMoneyException {
		SendRequest sendRequest;

		Assert.notNull(sourceAddress, "Source address is required");
		Assert.notNull(destinationAddress, "Destination address is required");

		if (sendAllAvailable) {
			sendRequest = SendRequest.emptyWallet(destinationAddress);
		} else {
			Assert.notNull(amount, "Amount is required when not sending all available cash");
			sendRequest = SendRequest.to(destinationAddress, amount);
		}

		if (changeAddress != null) {
			sendRequest.changeAddress = changeAddress;
		} else {
			sendRequest.changeAddress = sourceAddress;
		}

		sendRequest.coinSelector = new AddressCoinSelector(sourceAddress);
		sendRequest.signInputs = false;
		wallet.completeTx(sendRequest);

		return sendRequest;
	}
}
