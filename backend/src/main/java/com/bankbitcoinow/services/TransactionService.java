package com.bankbitcoinow.services;


import com.bankbitcoinow.models.Address;
import com.bankbitcoinow.models.Transaction;
import com.bankbitcoinow.models.TransactionStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.bankbitcoinow.repository.TransactionRepository;

import java.math.BigDecimal;
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
        List<Transaction> allTransactions = new ArrayList<>();
        transactionRepository.findAll().forEach(allTransactions::add);
        return  allTransactions;
    }

    public List<Transaction> getUserTransactions(List<Address> userAddresses){
        List<Transaction> userTransactions = new ArrayList<>();
        List<Transaction> allTransactions = getAllTransactions();

        for(Address address : userAddresses){
            for(Transaction transaction : allTransactions){
                if (address.getId().equals(transaction.getAddress().getId())){
                    userTransactions.add(transaction);
                }
            }
        }
        return userTransactions;
    }


    public double getAddressBalance(Long address_id){

        double balance = 0;

        List<Transaction> allTransactions = getAllTransactions();
        for(Transaction transaction : allTransactions){
            if (address_id.equals(transaction.getAddress().getId()) && transaction.getStatus() == TransactionStatus.CONFIRMED){
                balance += transaction.getAmount().doubleValue();
            }
        }

        return balance;
    }


}
