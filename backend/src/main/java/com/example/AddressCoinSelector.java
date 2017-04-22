package com.example;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.TransactionOutput;
import org.bitcoinj.script.Script;
import org.bitcoinj.wallet.CoinSelection;
import org.bitcoinj.wallet.CoinSelector;
import org.bitcoinj.wallet.DefaultCoinSelector;

import java.util.List;
import java.util.stream.Collectors;

class AddressCoinSelector implements CoinSelector {

	private final Address searchedAddress;
	private final CoinSelector delegate;

	public AddressCoinSelector(Address searchedAddress) {
		this(searchedAddress, new DefaultCoinSelector());
	}

	public AddressCoinSelector(Address searchedAddress, CoinSelector delegate) {
		this.searchedAddress = searchedAddress;
		this.delegate = delegate;
	}

	@Override
	public CoinSelection select(Coin target, List<TransactionOutput> candidates) {
		List<TransactionOutput> filteredCandidates = candidates.stream()
				.filter(this::isRelatedWithSearchedAddress)
				.collect(Collectors.toList());
		return delegate.select(target, filteredCandidates);
	}

	private boolean isRelatedWithSearchedAddress(TransactionOutput candidate) {
		Script script = candidate.getScriptPubKey();

		if (!hasAddress(script)) {
			return false;
		}

		Address toAddress = script.getToAddress(candidate.getParams(), true);
		return toAddress.equals(AddressCoinSelector.this.searchedAddress);
	}

	private boolean hasAddress(Script script) {
		return script.isSentToAddress()
				|| script.isPayToScriptHash()
				|| script.isSentToRawPubKey();
	}
}
