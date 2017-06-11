package com.bankbitcoinow.bitcoinj;

import com.bankbitcoinow.MockUtils;
import com.bankbitcoinow.models.TransactionStatus;
import com.bankbitcoinow.services.AddressService;
import com.bankbitcoinow.services.TransactionService;
import org.bitcoinj.core.AbstractBlockChain;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionOutput;
import org.bitcoinj.wallet.Wallet;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Random;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class TransactionUpdaterTest {

	@Mock private Wallet wallet;
	@Mock private AbstractBlockChain blockChain;
	@Mock private AddressService addressService;
	@Mock private TransactionService transactionService;
	@Captor private ArgumentCaptor<com.bankbitcoinow.models.Transaction> transactionCaptor;

	private TransactionUpdater transactionUpdater;

	@Before
	public void setUp() throws Exception {
		transactionUpdater = new TransactionUpdater(wallet, blockChain, addressService, transactionService);
	}

	@Test
	public void testListenerRegistration() throws Exception {
		transactionUpdater.run();

		verify(wallet, times(1)).addTransactionConfidenceEventListener(transactionUpdater);
	}

	@Test
	public void testNewIncomingTransaction() throws Exception {
		// Given
		Address address = TestUtils.newRandomAddress();
		com.bankbitcoinow.models.Address dbAddress = TestUtils.newDbAddressForBtcAddress(address);

		Transaction tx = new Transaction(TestUtils.PARAMS);
		TransactionOutput transactionOutputSpy = spy(new TransactionOutput(TestUtils.PARAMS, tx, Coin.valueOf(1234), address));
		tx.addOutput(transactionOutputSpy);

		when(transactionOutputSpy.isMineOrWatched(wallet)).thenReturn(true);
		when(addressService.findByAddress(address.toBase58())).thenReturn(dbAddress);

		// When
		transactionUpdater.onTransactionConfidenceChanged(wallet, tx);

		// Then
		MockUtils.printInvocationsInOrder(transactionOutputSpy, wallet, blockChain, addressService, transactionService);
		verify(transactionService, times(1)).addTransaction(transactionCaptor.capture());

		com.bankbitcoinow.models.Transaction dbTransaction = transactionCaptor.getValue();
		assertEquals(tx.getHashAsString(), dbTransaction.getHash());
		assertNull(dbTransaction.getSourceAddress());
		assertEquals(dbAddress.getAddress(), dbTransaction.getDestinationAddress());
		assertEquals(new BigDecimal("0.00001234"), dbTransaction.getAmount());
		assertEquals(TransactionStatus.UNCONFIRMED, dbTransaction.getStatus());
		assertEquals(0, dbTransaction.getConfirmations());
		assertSame(dbAddress, dbTransaction.getAddress());
	}

	@Test
	public void testExistingIncomingTransactionWithoutChange() throws Exception {
		// Given
		Address address = TestUtils.newRandomAddress();
		com.bankbitcoinow.models.Address dbAddress = TestUtils.newDbAddressForBtcAddress(address);

		Transaction tx = new Transaction(TestUtils.PARAMS);
		TransactionOutput transactionOutputSpy = spy(new TransactionOutput(TestUtils.PARAMS, tx, Coin.valueOf(1234), address));
		tx.addOutput(transactionOutputSpy);

		com.bankbitcoinow.models.Transaction dbTransaction = new com.bankbitcoinow.models.Transaction();
		dbTransaction.setId(new Random().nextLong());
		dbTransaction.setHash(tx.getHashAsString());
		dbTransaction.setDestinationAddress(dbAddress.getAddress());
		dbTransaction.setAmount(new BigDecimal("0.000012344"));
		dbTransaction.setCreatedAt(new Timestamp(System.currentTimeMillis()));
		dbTransaction.setStatus(TransactionStatus.UNCONFIRMED);
		dbTransaction.setConfirmations(0);
		dbTransaction.setBlockchainData(new byte[0]);
		dbTransaction.setAddress(dbAddress);

		when(transactionOutputSpy.isMineOrWatched(wallet)).thenReturn(true);
		when(addressService.findByAddress(address.toBase58())).thenReturn(dbAddress);
		when(transactionService.find(dbTransaction.getHash(), dbAddress.getAddress())).thenReturn(dbTransaction);

		// When
		transactionUpdater.onTransactionConfidenceChanged(wallet, tx);

		// Then
		MockUtils.printInvocationsInOrder(transactionOutputSpy, wallet, blockChain, addressService, transactionService);
		verify(transactionService, times(1)).find(dbTransaction.getHash(), dbAddress.getAddress());
		verifyNoMoreInteractions(transactionService);
	}

}