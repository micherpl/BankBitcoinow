package com.bankbitcoinow.controllers;

import com.bankbitcoinow.bitcoinj.BitcoinjFacade;
import com.bankbitcoinow.bitcoinj.EncryptedKey;
import com.bankbitcoinow.models.Address;
import com.bankbitcoinow.models.Transaction;
import com.bankbitcoinow.models.TransactionStatus;
import com.bankbitcoinow.services.AddressService;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.InsufficientMoneyException;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.wallet.SendRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;
import com.bankbitcoinow.services.TransactionService;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@RestController
public class TransactionController {

    private static final Logger LOG = LoggerFactory.getLogger(TransactionController.class);

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private AddressService addressService;

    @Autowired
    private BitcoinjFacade bitcoinjFacade;

    @Autowired
    private NetworkParameters networkParameters;

    private final ConcurrentMap<Long, SendRequest> preparedSendRequests = new ConcurrentHashMap<>();

    @RequestMapping(method = RequestMethod.POST, value="/nowy_przelew")
    public void doTransaction(@RequestBody Transaction transaction) {
        transactionService.addTransaction(transaction);

    }

    @RequestMapping(method = RequestMethod.POST, value="/przygotuj_przelew")
    public Map<String, Object> prepareTransaction(@RequestBody Map<String, String> input) throws InsufficientMoneyException {
        String from = input.get("from");
        Assert.hasText(from, "Source address cannot be empty");

        String to = input.get("to");
        Assert.hasText(to, "Destination address cannot be empty");

        String amountStr = input.get("amount");
        Assert.hasText(amountStr, "Amount cannot be empty");

        Coin amount = Coin.parseCoin(amountStr);
        Assert.isTrue(amount.isGreaterThan(Coin.ZERO), "Amount have to be greater than zero");

        Address srcAddress = addressService.findByAddress(from);
        if (srcAddress == null) {
            throw new IllegalArgumentException("Source address " + from + " was not found");
        }

        SendRequest sendRequest = bitcoinjFacade.prepareSendRequest()
                .from(bitcoinjFacade.getAddress(from))
                .to(bitcoinjFacade.getAddress(to))
                .amount(amount)
                .prepare();

        Transaction transaction = new Transaction();
        transaction.setAddress(srcAddress);
        transaction.setSourceAddress(from);
        transaction.setDestinationAddress(to);
        transaction.setAmount(amount.negate());
        transaction.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        transaction.setStatus(TransactionStatus.PREPARED);
        transaction.setConfirmations(0);
        transaction.setBlockchainData(sendRequest.tx.unsafeBitcoinSerialize());

        transaction = transactionService.addTransaction(transaction);
        LOG.info("Transaction created. ID: {}", transaction.getId());

        preparedSendRequests.put(transaction.getId(), sendRequest);

        Map<String, Object> result = new HashMap<>();
        result.put("id", transaction.getId());
        result.put("fee", sendRequest.tx.getFee().toPlainString());

        return result;
    }

    @RequestMapping(method = RequestMethod.POST, value="/podpisz_przelew")
    public void signTransaction(@RequestBody Map<String, String> input) throws IOException {
        String id = input.get("id");
        Assert.hasText(id, "ID cannot be empty");

        String password = input.get("password");
        Assert.hasText(password, "Password cannot be empty");

        Transaction transaction = transactionService.getTransaction(Long.parseLong(id));
        if (transaction == null) {
            throw new IllegalArgumentException("Transaction " + id + " was not found");
        }

        SendRequest sendRequest = preparedSendRequests.getOrDefault(transaction.getId(), null);
        if (sendRequest == null) {
            throw new IllegalArgumentException("Send request " + id + " was not found");
        }

        EncryptedKey encryptedKey = EncryptedKey.fromByteArray(transaction.getAddress().getPrivateKey());
        bitcoinjFacade.signSendRequest(sendRequest, encryptedKey, password);

        transaction.setHash(sendRequest.tx.getHashAsString());
        transaction.setStatus(TransactionStatus.SIGNED);
        transaction.setBlockchainData(sendRequest.tx.unsafeBitcoinSerialize());
        transactionService.updateTransction(transaction);
        preparedSendRequests.remove(transaction.getId());

        LOG.info("Transaction signed. ID: {}", transaction.getId());

        bitcoinjFacade.broadcastTransaction(sendRequest);
    }

    @RequestMapping(method = RequestMethod.GET, value="/wyswietl_wszystkie_transkacje")
    public void getAllTransaction(@RequestBody Transaction transaction) {
        transactionService.getAllTransactions();

    }
}
