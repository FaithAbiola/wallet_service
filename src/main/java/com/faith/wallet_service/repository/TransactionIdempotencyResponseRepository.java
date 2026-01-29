package com.faith.wallet_service.repository;

import com.faith.wallet_service.entity.TransactionIdempotencyResponse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TransactionIdempotencyResponseRepository extends JpaRepository<TransactionIdempotencyResponse, Long> {
    Optional<TransactionIdempotencyResponse> findByIdempotencyKey(String idempotencyKey);
}