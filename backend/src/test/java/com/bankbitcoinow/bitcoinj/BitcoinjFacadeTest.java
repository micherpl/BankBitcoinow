package com.bankbitcoinow.bitcoinj;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.PeerGroup;
import org.bitcoinj.core.TransactionBroadcast;
import org.bitcoinj.wallet.SendRequest;
import org.bitcoinj.wallet.Wallet;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.verification.VerificationMode;

import java.security.SecureRandom;
import java.util.Collections;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class BitcoinjFacadeTest {

	private static final NetworkParameters PARAMS = TestUtils.PARAMS;

	@Mock private PeerGroup peerGroup;
	@Mock private SingleKeyTransactionSigner transactionSigner;
	@Mock private Wallet wallet;
	private BitcoinjFacade bitcoinjFacade;

	@Before
	public void setUp() throws Exception {
		when(wallet.getTransactionSigners())
				.thenReturn(Collections.singletonList(transactionSigner));

		bitcoinjFacade = new BitcoinjFacade(TestUtils.PARAMS, wallet, peerGroup);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testCreatingWithIncorrectWallet() throws Exception {
		Wallet wallet = new Wallet(PARAMS);
		new BitcoinjFacade(TestUtils.PARAMS, wallet, peerGroup);
	}

	@Test
	public void testGenerateNewKey() throws Exception {
		byte[] seed = "seed".getBytes();
		ECKey key = new ECKey(new SecureRandom(seed));
		String password = "123456";

		bitcoinjFacade.setSecureRandom(new SecureRandom(seed));
		EncryptedKey generatedKey = bitcoinjFacade.generateNewKey(password);
		ECKey decryptedKey = generatedKey.decryptKey(password);

		assertEquals(key.toAddress(PARAMS).toBase58(), generatedKey.getAddress(PARAMS));
		assertEquals(key.getPubKeyPoint(), decryptedKey.getPubKeyPoint());
		assertEquals(key.getPrivKey(), decryptedKey.getPrivKey());
	}

	@Test
	public void testGetAddress() throws Exception {
		String addresBase58 = "mtHPuhhhXpyAk5FTG7mMa8K7ntUHARzXXn";
		Address address = bitcoinjFacade.getAddress(addresBase58);
		assertEquals(addresBase58, address.toBase58());
	}

	@Test
	public void testPrepareSendRequest() throws Exception {
		SendRequestBuilder sendRequestBuilder = bitcoinjFacade.prepareSendRequest();
		assertNotNull(sendRequestBuilder);
	}

	@Test
	public void testSignSendRequest() throws Exception {
		SendRequest sendRequest = TestUtils.newRandomSendRequest(wallet);
		EncryptedKey encryptedKey = mock(EncryptedKey.class);
		String password = "123456";
		ECKey key = TestUtils.newRandomKey();
		when(encryptedKey.decryptKey(password)).thenReturn(key);

		bitcoinjFacade.signSendRequest(sendRequest, encryptedKey, password);

		InOrder inOrder = inOrder(transactionSigner, wallet);
		inOrder.verify(transactionSigner, once()).setKey(key, null);
		inOrder.verify(wallet, once()).signTransaction(sendRequest);
		inOrder.verify(transactionSigner, once()).clearKey();
	}

	@Test
	public void testClearingKeyWhenExceptionOccurs() throws Exception {
		SendRequest sendRequest = TestUtils.newRandomSendRequest(wallet);
		EncryptedKey encryptedKey = mock(EncryptedKey.class);
		String password = "123456";
		when(encryptedKey.decryptKey(password)).thenThrow(new RuntimeException("Test exception"));

		try {
			bitcoinjFacade.signSendRequest(sendRequest, encryptedKey, password);
		} catch (RuntimeException e) {
			if (e.getMessage().equals("Test exception")) {
				verify(transactionSigner, once()).clearKey();
			} else {
				throw e;
			}
		}
	}

	private VerificationMode once() {
		return times(1);
	}

	@Test
	public void testBroadcastTransaction() throws Exception {
		SendRequest sendRequest = TestUtils.newRandomSendRequest(wallet);
		TransactionBroadcast fakeTransactionBroadcast = mock(TransactionBroadcast.class);
		when(peerGroup.broadcastTransaction(sendRequest.tx)).thenReturn(fakeTransactionBroadcast);

		TransactionBroadcast transactionBroadcast = bitcoinjFacade.broadcastTransaction(sendRequest);

		assertSame(fakeTransactionBroadcast, transactionBroadcast);
	}
}