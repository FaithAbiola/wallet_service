package com.faith.wallet_service.repository;

import com.faith.wallet_service.entity.TransferTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TransferTransactionRepository extends JpaRepository<TransferTransaction, Long> {
    Optional<TransferTransaction> findByIdempotencyKey(String idempotencyKey);
}