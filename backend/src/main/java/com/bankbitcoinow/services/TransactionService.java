package com.bankbitcoinow.services;


import com.bankbitcoinow.models.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.bankbitcoinow.repository.TransactionRepository;

import java.util.ArrayList;
import java.util.List;

@Service
public class TransactionService {
    @Autowired
    private TransactionRepository transactionRepository;

    public Transaction addTransaction(Transaction transaction){
        return transactionRepository.save(transaction);
    }

    public void updateTransction(Transaction transaction){
        transactionRepository.save(transaction);
    }

    public Transaction getTransaction(Long id){
        return transactionRepository.findOne(id);
    }

    public Transaction find(String hash, String address) {
        return transactionRepository.findByHashAndAddressAddress(hash, address);
    }

    public void deleteTransaction(Long id){
        transactionRepository.delete(id);
    }

    public List<Transaction> getAllTransactions(){
        List<Transaction> transactions = new ArrayList<>();
        transactionRepository.findAll().forEach(transactions::add);
        return  transactions;
    }

}
