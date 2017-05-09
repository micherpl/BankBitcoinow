package com.bankbitcoinow.repository;


import com.bankbitcoinow.models.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<Transaction, Long>{

	// TODO Transaction findByHashAndDestinationAddress(String hash, String address);
}