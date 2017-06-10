package com.bankbitcoinow.bitcoinj;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.Context;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.TransactionOutput;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.script.Script;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Random;

class TestUtils {

	static final NetworkParameters PARAMS = TestNet3Params.get();
	static final Random RANDOM = new Random();

	static {
		// Have to be created before using BitcoinJ
		Context.getOrCreate(PARAMS);
	}

	private TestUtils() {
	}

	static Address newRandomAddress() {
		return newRandomKey().toAddress(PARAMS);
	}

	static ECKey newRandomKey() {
		return new ECKey();
	}

	static com.bankbitcoinow.models.Address newDbAddressForBtcAddress(Address address) {
		com.bankbitcoinow.models.Address dbAddress = new com.bankbitcoinow.models.Address();
		dbAddress.setId(new Random().nextLong());
		dbAddress.setAddress(address.toBase58());
		dbAddress.setPrivateKey(new byte[0]);
		dbAddress.setBalance(BigDecimal.ZERO);
		dbAddress.setCreated_at(new Timestamp(System.currentTimeMillis()));
		return dbAddress;
	}

	static TransactionOutput newTransactionOutput(Script script) {
		return new TransactionOutput(PARAMS, null, randomCoin(), script.getProgram());
	}

	static Coin randomCoin() {
		return Coin.valueOf(Math.abs(RANDOM.nextInt()));
	}
}
