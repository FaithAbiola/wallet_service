package com.faith.wallet_service.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "transfer_idempotency_responses")
public class TransferIdempotencyResponse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "idempotency_key", nullable = false, unique = true)
    private String idempotencyKey;

    // Stored response data
    private Long transferId;
    private Long fromWalletId;
    private Long fromBalance;
    private Long toWalletId;
    private Long toBalance;
    private LocalDateTime timestamp;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}