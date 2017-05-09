package services;


import models.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import repository.TransactionRepository;

import java.util.ArrayList;
import java.util.List;

public class TransactionService {
    @Autowired
    private TransactionRepository transactionRepository;

    public void addTransaction(Transaction transaction){
        transactionRepository.save(transaction);
    }

    public void updateTransction(Transaction transaction){
        transactionRepository.save(transaction);
    }

    public void getTransaction(Long id){
        transactionRepository.findOne(id);
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
