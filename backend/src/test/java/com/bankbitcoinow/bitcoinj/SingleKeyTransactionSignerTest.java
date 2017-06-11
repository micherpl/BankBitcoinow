package com.bankbitcoinow.bitcoinj;

import org.bitcoinj.core.ECKey;
import org.bitcoinj.crypto.KeyCrypterScrypt;
import org.bitcoinj.signers.TransactionSigner.ProposedTransaction;
import org.bitcoinj.wallet.KeyBag;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.spongycastle.crypto.params.KeyParameter;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class SingleKeyTransactionSignerTest {

	@Mock private ProposedTransaction proposedTransaction;
	@Mock private KeyBag keyBag;
	private SingleKeyTransactionSigner transactionSigner;

	@Before
	public void setUp() throws Exception {
		transactionSigner = new SingleKeyTransactionSigner();
	}

	@Test
	public void testIsReady() throws Exception {
		assertTrue(transactionSigner.isReady());
	}

	@Test(expected = IllegalArgumentException.class)
	public void testSettingNullKey() throws Exception {
		transactionSigner.setKey(null, null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testSettingKeyWithoutPrivatePart() throws Exception {
		ECKey key = TestUtils.newRandomKey();
		ECKey pubKey = ECKey.fromPublicOnly(key.getPubKeyPoint());

		transactionSigner.setKey(pubKey, null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testSettingEncryptedKeyWithoutAesKey() throws Exception {
		ECKey key = TestUtils.newRandomKey();
		ECKey encryptedKey = key.encrypt(new KeyCrypterScrypt(), new KeyParameter(new byte[32]));

		transactionSigner.setKey(encryptedKey, null);
	}

	@Test
	public void testSettingEncryptedKeyWithAesKey() throws Exception {
		ECKey key = TestUtils.newRandomKey();
		KeyParameter aesKey = new KeyParameter(new byte[32]);
		ECKey encryptedKey = key.encrypt(new KeyCrypterScrypt(), aesKey);

		transactionSigner.setKey(encryptedKey, aesKey);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testSigningWithoutKeySet() throws Exception {
		transactionSigner.signInputs(proposedTransaction, keyBag);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testSigningAfterClearingKey() throws Exception {
		ECKey key = TestUtils.newRandomKey();

		transactionSigner.setKey(key, null);
		transactionSigner.clearKey();
		transactionSigner.signInputs(proposedTransaction, keyBag);
	}

}