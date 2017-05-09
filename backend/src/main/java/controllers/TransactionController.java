package controllers;

import models.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import services.TransactionService;

@RestController
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @RequestMapping(method = RequestMethod.POST, value="/nowy_przelew")
    public void doTransaction(@RequestBody Transaction transaction) {
        transactionService.addTransaction(transaction);

    }

    @RequestMapping(method = RequestMethod.GET, value="/wyswietl_wszystkie_transkacje")
    public void getAllTransaction(@RequestBody Transaction transaction) {
        transactionService.getAllTransactions();

    }
}
