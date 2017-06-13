package com.bankbitcoinow.models;

import org.junit.Test;

import static org.junit.Assert.*;

public class TransactionTest {

	@Test
	public void testIsIncoming() throws Exception {
		Address address = new Address();
		address.setAddress("mtHPuhhhXpyAk5FTG7mMa8K7ntUHARzXXn");

		Transaction transaction = new Transaction();
		transaction.setAddress(address);
		transaction.setDestinationAddress(address.getAddress());

		assertTrue(transaction.isIncoming());
		assertFalse(transaction.isOutgoing());
	}

	@Test
	public void testIsOutgoing() throws Exception {
		Address address = new Address();
		address.setAddress("mtHPuhhhXpyAk5FTG7mMa8K7ntUHARzXXn");

		Transaction transaction = new Transaction();
		transaction.setAddress(address);
		transaction.setSourceAddress(address.getAddress());

		assertFalse(transaction.isIncoming());
		assertTrue(transaction.isOutgoing());
	}
}