package com.bankbitcoinow.bitcoinj;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.Context;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.params.TestNet3Params;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Random;

class TestUtils {

	static final NetworkParameters PARAMS = TestNet3Params.get();

	static {
		// Have to be created before using BitcoinJ
		Context.getOrCreate(PARAMS);
	}

	private TestUtils() {
	}

	static Address newRandomAddress() {
		return new ECKey().toAddress(PARAMS);
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
}
