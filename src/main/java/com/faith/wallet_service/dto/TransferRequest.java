package com.faith.wallet_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class TransferRequest {
    @NotNull(message = "Sender wallet ID is required")
    private Long fromWalletId;

    @NotNull(message = "Receiver wallet ID is required")
    private Long toWalletId;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private Long amount;

    @NotBlank(message = "Idempotency key is required")
    private String idempotencyKey;
}
