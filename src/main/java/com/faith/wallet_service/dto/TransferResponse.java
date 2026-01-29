package com.faith.wallet_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransferResponse {
    private Long transferId;
    private Long fromWalletId;
    private Long fromBalance;
    private Long toWalletId;
    private Long toBalance;
    private LocalDateTime timestamp;
}
