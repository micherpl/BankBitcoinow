package com.bankbitcoinow.repository;

import com.bankbitcoinow.models.Contact;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContactRepository extends JpaRepository<Contact, Long> {
}
