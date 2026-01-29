package com.faith.wallet_service.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "transaction_idempotency_responses")
public class TransactionIdempotencyResponse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "idempotency_key", nullable = false, unique = true)
    private String idempotencyKey;

    // Stored response data
    private Long transactionId;
    private Long walletId;
    private Long balance;
    private LocalDateTime timestamp;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}