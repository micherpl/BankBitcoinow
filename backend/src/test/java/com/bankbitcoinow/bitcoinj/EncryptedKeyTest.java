package com.bankbitcoinow.bitcoinj;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.crypto.KeyCrypterException;
import org.junit.Before;
import org.junit.Test;
import org.spongycastle.util.encoders.Hex;

import static org.junit.Assert.*;

public class EncryptedKeyTest {

	private static final NetworkParameters PARAMS = TestUtils.PARAMS;

	private ECKey key;
	private Address address;
	private String password;

	@Before
	public void setUp() throws Exception {
		key = TestUtils.newRandomKey();
		address = key.toAddress(TestUtils.PARAMS);
		password = "123456";
	}

	@Test
	public void testEncryptionAndDecryption() throws Exception {
		EncryptedKey encryptedKey = EncryptedKey.encryptKey(key, password);
		ECKey decryptedKey = encryptedKey.decryptKey(password);

		assertEquals(address.toBase58(), encryptedKey.getAddress(PARAMS));
		assertNotSame(key, decryptedKey);
		assertEquals(key, decryptedKey);
	}

	@Test(expected = KeyCrypterException.class)
	public void testDecryptingWithWrongPassword() throws Exception {
		EncryptedKey encryptedKey = EncryptedKey.encryptKey(key, password);
		encryptedKey.decryptKey(password + "something");
	}

	@Test
	public void testSerializationAndDeserialization() throws Exception {
		EncryptedKey encryptedKey = EncryptedKey.encryptKey(key, password);
		byte[] bytes = encryptedKey.toByteArray();
		EncryptedKey encryptedKey2 = EncryptedKey.fromByteArray(bytes);
		System.out.println(Hex.toHexString(bytes));

		assertEquals(address.toBase58(), encryptedKey2.getAddress(PARAMS));
	}

	@Test
	public void testDeserialization() throws Exception {
		byte[] bytes = Hex.decode("aced0005778100000000000100000000000872c2039b0140601a00000010afff9a439c07d9243d5727c52f99b1fe000000304009a637e3b5b12b4f893e7b494e8fefa5538fd058c88afbad30c09dcd4ad96608f11004a4d3266c3258d4b507b4c488000000210386be77c8405283d90c1a373382c606c60290597900d2035f8f97bea9ad4203d8");
		EncryptedKey encryptedKey = EncryptedKey.fromByteArray(bytes);

		assertEquals("mu7mmXvUYC8XbEV9QzAasLW2pYv5zK5zG9", encryptedKey.getAddress(TestUtils.PARAMS));
	}

	@Test(expected = IllegalStateException.class)
	public void testDeserializationWithTooShortSalt() throws Exception {
		byte[] bytes = Hex.decode("aced0005778100000000000100000000000872c2039b0140");
		EncryptedKey.fromByteArray(bytes);
	}

	@Test(expected = IllegalStateException.class)
	public void testDeserializationWithTooShortIV() throws Exception {
		byte[] bytes = Hex.decode("aced0005778100000000000100000000000872c2039b0140601a00000010afff9a439c07d9243d5727c5");
		EncryptedKey.fromByteArray(bytes);
	}

	@Test(expected = IllegalStateException.class)
	public void testDeserializationWithTooShortEncryptedBytes() throws Exception {
		byte[] bytes = Hex.decode("aced0005778100000000000100000000000872c2039b0140601a00000010afff9a439c07d9243d5727c52f99b1fe000000304009a6");
		EncryptedKey.fromByteArray(bytes);
	}

	@Test(expected = IllegalStateException.class)
	public void testDeserializationWithTooShortPubKey() throws Exception {
		byte[] bytes = Hex.decode("aced0005778100000000000100000000000872c2039b0140601a00000010afff9a439c07d9243d5727c52f99b1fe000000304009a637e3b5b12b4f893e7b494e8fefa5538fd058c88afbad30c09dcd4ad96608f11004a4d3266c3258d4b507b4c488000000210386be77c8");
		EncryptedKey.fromByteArray(bytes);
	}
}