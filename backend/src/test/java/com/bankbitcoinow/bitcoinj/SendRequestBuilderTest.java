package com.bankbitcoinow.bitcoinj;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;
import org.bitcoinj.wallet.SendRequest;
import org.bitcoinj.wallet.Wallet;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class SendRequestBuilderTest {

	@Mock private Wallet wallet;

	@Test
	public void testBasicProperties() throws Exception {
		SendRequest sendRequest = TestUtils.newRandomSendRequest(wallet);

		assertFalse(sendRequest.emptyWallet);
		assertFalse(sendRequest.signInputs);
		assertEquals(Wallet.MissingSigsMode.USE_DUMMY_SIG, sendRequest.missingSigsMode);

		verify(wallet, times(1)).completeTx(sendRequest);
	}

	@Test
	public void testEmptyingWallet() throws Exception {
		SendRequest sendRequest = new SendRequestBuilder(wallet)
				.from(TestUtils.newRandomAddress())
				.to(TestUtils.newRandomAddress())
				.allAvailable()
				.prepare();

		assertTrue(sendRequest.emptyWallet);
	}

	@Test
	public void testSettingDefaultChangeAddress() throws Exception {
		Address sourceAddress = TestUtils.newRandomAddress();

		SendRequest sendRequest = new SendRequestBuilder(wallet)
				.from(sourceAddress)
				.to(TestUtils.newRandomAddress())
				.amount(Coin.COIN)
				.prepare();

		assertEquals(sourceAddress, sendRequest.changeAddress);
	}

	@Test
	public void testCustomChangeAddress() throws Exception {
		Address sourceAddress = TestUtils.newRandomAddress();
		Address changeAddress = TestUtils.newRandomAddress();

		SendRequest sendRequest = new SendRequestBuilder(wallet)
				.from(sourceAddress)
				.to(TestUtils.newRandomAddress())
				.change(changeAddress)
				.amount(Coin.COIN)
				.prepare();

		assertEquals(changeAddress, sendRequest.changeAddress);
	}

	@Test
	public void testSettingCoinSelector() throws Exception {
		Address sourceAddress = TestUtils.newRandomAddress();
		SendRequest sendRequest = new SendRequestBuilder(wallet)
				.from(sourceAddress)
				.to(TestUtils.newRandomAddress())
				.amount(Coin.COIN)
				.prepare();

		assertTrue(sendRequest.coinSelector instanceof AddressCoinSelector);
		AddressCoinSelector addressCoinSelector = (AddressCoinSelector) sendRequest.coinSelector;
		assertEquals(sourceAddress, addressCoinSelector.searchedAddress);
	}

	@Test
	public void testCreatingOutputs() throws Exception {
		Address destinationAddress = TestUtils.newRandomAddress();
		Coin amount = Coin.COIN;

		SendRequest sendRequest = new SendRequestBuilder(wallet)
				.from(TestUtils.newRandomAddress())
				.to(destinationAddress)
				.amount(amount)
				.prepare();

		assertEquals(1, sendRequest.tx.getOutputs().size());
		assertEquals(amount, sendRequest.tx.getOutput(0).getValue());
		assertEquals(destinationAddress, sendRequest.tx.getOutputs().get(0).getAddressFromP2PKHScript(TestUtils.PARAMS));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testPreparingWithoutAnyData() throws Exception {
		new SendRequestBuilder(wallet).prepare();
	}

	@Test(expected = IllegalArgumentException.class)
	public void testErrorWhenSourceIsNull() throws Exception {
		new SendRequestBuilder(wallet)
				.from(null)
				.to(TestUtils.newRandomAddress())
				.amount(Coin.COIN)
				.prepare();
	}

	@Test(expected = IllegalArgumentException.class)
	public void testErrorWhenDestinationIsNull() throws Exception {
		new SendRequestBuilder(wallet)
				.from(TestUtils.newRandomAddress())
				.to(null)
				.amount(Coin.COIN)
				.prepare();
	}

	@Test(expected = IllegalArgumentException.class)
	public void testErrorWhenAmountIsNull() throws Exception {
		new SendRequestBuilder(wallet)
				.from(TestUtils.newRandomAddress())
				.to(TestUtils.newRandomAddress())
				.amount(null)
				.prepare();
	}

	@Test(expected = IllegalStateException.class)
	public void testErrorWhenTyingToEmptyWalletAndSetAmount() throws Exception {
		new SendRequestBuilder(wallet)
				.allAvailable()
				.amount(Coin.COIN);
	}

	@Test(expected = IllegalStateException.class)
	public void testErrorWhenTyingToSetAmountAndEmptyWallet() throws Exception {
		new SendRequestBuilder(wallet)
				.amount(Coin.COIN)
				.allAvailable();
	}

}