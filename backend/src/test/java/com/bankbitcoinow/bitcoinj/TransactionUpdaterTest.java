package com.bankbitcoinow.bitcoinj;

import com.bankbitcoinow.MockUtils;
import com.bankbitcoinow.models.TransactionStatus;
import com.bankbitcoinow.services.AddressService;
import com.bankbitcoinow.services.TransactionService;
import org.bitcoinj.core.AbstractBlockChain;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionConfidence;
import org.bitcoinj.core.TransactionConfidence.ConfidenceType;
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

	@Test
	public void testUpdateExistingTransactionWithChangedStatus() throws Exception {
		// Given
		Transaction tx = mock(Transaction.class);
		when(tx.getConfidence()).thenAnswer(invocation -> {
			TransactionConfidence confidence = new TransactionConfidence(Sha256Hash.ZERO_HASH);
			confidence.setConfidenceType(ConfidenceType.PENDING);
			return confidence;
		});

		com.bankbitcoinow.models.Transaction dbTransaction = new com.bankbitcoinow.models.Transaction();
		dbTransaction.setId(123L);
		dbTransaction.setStatus(TransactionStatus.SIGNED);

		// When
		transactionUpdater.updateExistingTransaction(tx, dbTransaction);

		// Then
		verify(transactionService, times(1)).updateTransction(transactionCaptor.capture());
		com.bankbitcoinow.models.Transaction updatedTransaction = transactionCaptor.getValue();
		assertEquals(Long.valueOf(123), updatedTransaction.getId());
		assertEquals(TransactionStatus.UNCONFIRMED, updatedTransaction.getStatus());
		assertEquals(0, updatedTransaction.getConfirmations());
	}

	@Test
	public void testUpdateExistingTransactionWithChangedConfirmations() throws Exception {
		// Given
		Transaction tx = mock(Transaction.class);
		when(tx.getConfidence()).thenAnswer(invocation -> {
			TransactionConfidence confidence = new TransactionConfidence(Sha256Hash.ZERO_HASH);
			confidence.setConfidenceType(ConfidenceType.BUILDING);
			confidence.setAppearedAtChainHeight(1000);
			return confidence;
		});

		when(wallet.getLastBlockSeenHeight()).thenReturn(1201);

		com.bankbitcoinow.models.Transaction dbTransaction = new com.bankbitcoinow.models.Transaction();
		dbTransaction.setId(123L);
		dbTransaction.setStatus(TransactionStatus.CONFIRMED);
		dbTransaction.setConfirmations(200);

		// When
		transactionUpdater.updateExistingTransaction(tx, dbTransaction);

		// Then
		verify(transactionService, times(1)).updateTransction(transactionCaptor.capture());
		com.bankbitcoinow.models.Transaction updatedTransaction = transactionCaptor.getValue();
		assertEquals(Long.valueOf(123), updatedTransaction.getId());
		assertEquals(TransactionStatus.CONFIRMED, updatedTransaction.getStatus());
		assertEquals(201, updatedTransaction.getConfirmations());
	}

	@Test
	public void testAddNewTransactionForNonExistingAddress() throws Exception {
		String addressStr = "mtHPuhhhXpyAk5FTG7mMa8K7ntUHARzXXn";
		when(addressService.findByAddress(addressStr)).thenReturn(null);

		transactionUpdater.addNewTransaction(new Transaction(TestUtils.PARAMS), Coin.COIN, false, addressStr);

		verifyZeroInteractions(transactionService);
	}

	@Test
	public void testAddNewTransaction() throws Exception {
		// Given
		String addressStr = "mtHPuhhhXpyAk5FTG7mMa8K7ntUHARzXXn";
		com.bankbitcoinow.models.Address dbAddress = new com.bankbitcoinow.models.Address();
		dbAddress.setAddress(addressStr);
		when(addressService.findByAddress(addressStr)).thenReturn(dbAddress);

		Transaction tx = mock(Transaction.class);

		String txHash = "some hash";
		when(tx.getHashAsString()).thenReturn(txHash);

		byte[] txData = {0x11, 0x22, 0x33};
		when(tx.unsafeBitcoinSerialize()).thenReturn(txData);

		when(tx.getConfidence()).thenAnswer(invocation -> {
			TransactionConfidence confidence = new TransactionConfidence(Sha256Hash.ZERO_HASH);
			confidence.setConfidenceType(ConfidenceType.BUILDING);
			confidence.setAppearedAtChainHeight(100);
			return confidence;
		});

		when(wallet.getLastBlockSeenHeight()).thenReturn(333);

		// When
		transactionUpdater.addNewTransaction(tx, Coin.COIN, false, addressStr);

		// Then
		verify(transactionService, times(1)).addTransaction(transactionCaptor.capture());
		com.bankbitcoinow.models.Transaction dbTransaction = transactionCaptor.getValue();
		assertEquals(txHash, dbTransaction.getHash());
		assertEquals(dbAddress.getAddress(), dbTransaction.getSourceAddress());
		assertNull(dbTransaction.getDestinationAddress());
		assertEquals(new BigDecimal("1.00000000"), dbTransaction.getAmount());
		assertEquals(TransactionStatus.CONFIRMED, dbTransaction.getStatus());
		assertEquals(233, dbTransaction.getConfirmations());
		assertArrayEquals(txData, dbTransaction.getBlockchainData());
		assertSame(dbAddress, dbTransaction.getAddress());
	}

	@Test
	public void testGetNewStatusNullUnknown() throws Exception {
		testStatus(null, ConfidenceType.UNKNOWN, null);
	}

	@Test
	public void testGetNewStatusPreparedUnknown() throws Exception {
		testStatus(TransactionStatus.PREPARED, ConfidenceType.UNKNOWN, TransactionStatus.PREPARED);
	}

	@Test
	public void testGetNewStatusSignedPending() throws Exception {
		testStatus(TransactionStatus.SIGNED, ConfidenceType.PENDING, TransactionStatus.UNCONFIRMED);
	}

	@Test
	public void testGetNewStatusUnconfirmedPending() throws Exception {
		testStatus(TransactionStatus.UNCONFIRMED, ConfidenceType.PENDING, TransactionStatus.UNCONFIRMED);
	}

	@Test
	public void testGetNewStatusUnconfirmedBuilding() throws Exception {
		testStatus(TransactionStatus.UNCONFIRMED, ConfidenceType.BUILDING, TransactionStatus.CONFIRMED);
	}

	@Test
	public void testGetNewStatusConfirmedBuilding() throws Exception {
		testStatus(TransactionStatus.CONFIRMED, ConfidenceType.BUILDING, TransactionStatus.CONFIRMED);
	}

	private void testStatus(TransactionStatus currentStatus,
	                        ConfidenceType confidenceType,
	                        TransactionStatus expectedStatus) {
		TransactionConfidence confidence = new TransactionConfidence(Sha256Hash.ZERO_HASH);
		confidence.setConfidenceType(confidenceType);

		TransactionStatus newStatus = TransactionUpdater.getNewStatus(currentStatus, confidence);

		assertEquals(expectedStatus, newStatus);
	}

	@Test
	public void testUpdateConfirmations() throws Exception {
		// Given
		com.bankbitcoinow.models.Transaction transaction = mock(com.bankbitcoinow.models.Transaction.class);
		when(transaction.getStatus()).thenReturn(TransactionStatus.CONFIRMED);

		TransactionConfidence confidence = new TransactionConfidence(Sha256Hash.ZERO_HASH);
		confidence.setConfidenceType(ConfidenceType.BUILDING);
		confidence.setAppearedAtChainHeight(123);

		when(wallet.getLastBlockSeenHeight()).thenReturn(234);

		// When
		transactionUpdater.updateConfirmations(transaction, confidence);

		// Then
		verify(transaction, times(1)).setConfirmations(111);
	}
}