package com.bankbitcoinow.bitcoinj;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.TransactionOutput;
import org.bitcoinj.script.ScriptBuilder;
import org.bitcoinj.wallet.CoinSelection;
import org.bitcoinj.wallet.CoinSelector;
import org.bitcoinj.wallet.DefaultCoinSelector;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.*;

public class AddressCoinSelectorTest {

	private final CoinSelection fakeCoinSelection = new CoinSelection(Coin.ZERO, Collections.emptyList());

	@Test
	public void testCreatingWithDefaultCoinSelector() throws Exception {
		Address searchedAddress = TestUtils.newRandomAddress();
		AddressCoinSelector addressCoinSelector = new AddressCoinSelector(searchedAddress);

		assertSame(searchedAddress, addressCoinSelector.searchedAddress);
		assertTrue(addressCoinSelector.delegate instanceof DefaultCoinSelector);
	}

	@Test
	public void testCreatingWithCustomCoinSelector() throws Exception {
		Address searchedAddress = TestUtils.newRandomAddress();
		CoinSelector customCoinSelector = (target, candidates) -> null;
		AddressCoinSelector addressCoinSelector = new AddressCoinSelector(searchedAddress, customCoinSelector);

		assertSame(searchedAddress, addressCoinSelector.searchedAddress);
		assertSame(customCoinSelector, addressCoinSelector.delegate);
	}

	@Test
	public void testSelectingCorrectTransactinOutputs() throws Exception {
		Address address = TestUtils.newRandomAddress();
		TransactionOutput transactionOutput1 = TestUtils.newTransactionOutput(ScriptBuilder.createOutputScript(TestUtils.newRandomAddress()));
		TransactionOutput transactionOutput2 = TestUtils.newTransactionOutput(ScriptBuilder.createOutputScript(address));
		TransactionOutput transactionOutput3 = TestUtils.newTransactionOutput(ScriptBuilder.createOutputScript(TestUtils.newRandomAddress()));

		AddressCoinSelector addressCoinSelector = new AddressCoinSelector(address, (target, candidates) -> {
			assertEquals(1, candidates.size());
			assertSame(transactionOutput2, candidates.get(0));
			return fakeCoinSelection;
		});
		CoinSelection coinSelection = addressCoinSelector.select(TestUtils.randomCoin(), Arrays.asList(transactionOutput1, transactionOutput2, transactionOutput3));

		assertSame(fakeCoinSelection, coinSelection);
	}

	@Test
	public void testIgnoringMultiSigTransactinOutputs() throws Exception {
		ECKey key = TestUtils.newRandomKey();
		Address address = key.toAddress(TestUtils.PARAMS);
		TransactionOutput transactionOutput1 = TestUtils.newTransactionOutput(ScriptBuilder.createOutputScript(key));
		TransactionOutput transactionOutput2 = TestUtils.newTransactionOutput(ScriptBuilder.createMultiSigOutputScript(2, Arrays.asList(key, TestUtils.newRandomKey())));

		AddressCoinSelector addressCoinSelector = new AddressCoinSelector(address, (target, candidates) -> {
			assertEquals(1, candidates.size());
			assertSame(transactionOutput1, candidates.get(0));
			return fakeCoinSelection;
		});
		CoinSelection coinSelection = addressCoinSelector.select(TestUtils.randomCoin(), Arrays.asList(transactionOutput1, transactionOutput2));

		assertSame(fakeCoinSelection, coinSelection);
	}

}