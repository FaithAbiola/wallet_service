package com.faith.wallet_service.service;

import com.faith.wallet_service.dto.TransactionRequest;
import com.faith.wallet_service.dto.TransactionResponse;
import com.faith.wallet_service.dto.TransferRequest;
import com.faith.wallet_service.dto.TransferResponse;

public interface TransactionService {
    TransactionResponse applyTransaction(TransactionRequest request);
    TransferResponse transfer(TransferRequest request);
}
