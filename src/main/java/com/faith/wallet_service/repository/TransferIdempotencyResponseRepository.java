package com.faith.wallet_service.repository;

import com.faith.wallet_service.entity.TransferIdempotencyResponse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TransferIdempotencyResponseRepository extends JpaRepository<TransferIdempotencyResponse, Long> {
    Optional<TransferIdempotencyResponse> findByIdempotencyKey(String idempotencyKey);
}