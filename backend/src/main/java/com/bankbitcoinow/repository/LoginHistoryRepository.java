package com.bankbitcoinow.repository;

import com.bankbitcoinow.models.LoginHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoginHistoryRepository extends JpaRepository<LoginHistory, Long> {
}
