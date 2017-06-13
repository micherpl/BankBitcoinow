package com.bankbitcoinow.repository;

import com.bankbitcoinow.models.Address;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AddressRepository extends JpaRepository<Address, Long> {
	Address findByAddress(String address);
}
