package com.bankbitcoinow.repository;


import com.bankbitcoinow.models.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long>{

	Transaction findByHashAndAddressAddress(String hash, String address);
	List<Transaction> findByAddressId(Long id);
}
