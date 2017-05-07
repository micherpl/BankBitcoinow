package com.bankbitcoinow.bitcoinj;

import com.google.protobuf.ByteString;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.crypto.EncryptedData;
import org.bitcoinj.crypto.KeyCrypter;
import org.bitcoinj.crypto.KeyCrypterScrypt;
import org.bitcoinj.wallet.Protos;
import org.spongycastle.crypto.params.KeyParameter;
import org.springframework.util.Assert;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class EncryptedKey {

	private final long iterations;
	private final byte[] salt;
	private final ECKey key;

	/**
	 * Use {@link #encryptKey(ECKey, String)} or {@link #fromByteArray(byte[])},
	 */
	private EncryptedKey(long iterations, byte[] salt, ECKey key) {
		Assert.isTrue(key.isEncrypted(), "ECKey is not encrypted");

		this.iterations = iterations;
		this.salt = salt;
		this.key = key;
	}

	public static EncryptedKey encryptKey(ECKey key, String password) {
		int iterations = 65536;
		byte[] salt = KeyCrypterScrypt.randomSalt();
		KeyCrypter keyCrypter = getKeyCrypter(iterations, salt);
		KeyParameter aesKey = keyCrypter.deriveKey(password);
		ECKey encryptedKey = key.encrypt(keyCrypter, aesKey);
		return new EncryptedKey(iterations, salt, encryptedKey);
	}

	public static EncryptedKey fromByteArray(byte[] bytes) throws IOException {
		try (ObjectInputStream input = new ObjectInputStream(new ByteArrayInputStream(bytes))) {
			long iterations = input.readLong();

			byte[] salt = new byte[input.readInt()];
			Assert.state(input.read(salt) == salt.length, "Read salt is too short");

			byte[] iv = new byte[input.readInt()];
			Assert.state(input.read(iv) == iv.length, "Read IV is too short");

			byte[] encryptedBytes = new byte[input.readInt()];
			Assert.state(input.read(encryptedBytes) == encryptedBytes.length, "Read encrypted bytes are too short");

			byte[] pubKey = new byte[input.readInt()];
			Assert.state(input.read(pubKey) == pubKey.length, "Read public key is too short");

			KeyCrypter keyCrypter = getKeyCrypter(iterations, salt);
			EncryptedData encryptedData = new EncryptedData(iv, encryptedBytes);
			ECKey key = ECKey.fromEncrypted(encryptedData, keyCrypter, pubKey);

			return new EncryptedKey(iterations, salt, key);
		}
	}

	public ECKey decryptKey(String password) {
		KeyCrypter keyCrypter = key.getKeyCrypter();
		if (keyCrypter == null) {
			keyCrypter = getKeyCrypter(iterations, salt);
		}

		KeyParameter aesKey = keyCrypter.deriveKey(password);
		return key.decrypt(keyCrypter, aesKey);
	}

	public byte[] toByteArray() throws IOException {
		try (ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		     ObjectOutputStream output = new ObjectOutputStream(buffer)) {
			EncryptedData encryptedData = key.getEncryptedData();
			byte[] pubKey = key.getPubKey();

			output.writeLong(iterations);

			output.writeInt(salt.length);
			output.write(salt);

			output.writeInt(encryptedData.initialisationVector.length);
			output.write(encryptedData.initialisationVector);

			output.writeInt(encryptedData.encryptedBytes.length);
			output.write(encryptedData.encryptedBytes);

			output.writeInt(pubKey.length);
			output.write(pubKey);

			output.flush();

			return buffer.toByteArray();
		}
	}

	private static KeyCrypter getKeyCrypter(long iterations, byte[] salt) {
		Protos.ScryptParameters scryptParameters = Protos.ScryptParameters.newBuilder()
				.setN(iterations)
				.setSalt(ByteString.copyFrom(salt))
				.build();
		return new KeyCrypterScrypt(scryptParameters);
	}
}
