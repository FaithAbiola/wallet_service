package com.faith.wallet_service.controller;

import com.faith.wallet_service.commons.ResultWrapper;
import com.faith.wallet_service.dto.TransactionRequest;
import com.faith.wallet_service.dto.TransactionResponse;
import com.faith.wallet_service.dto.TransferRequest;
import com.faith.wallet_service.dto.TransferResponse;
import com.faith.wallet_service.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/transactions")
@Tag(name = "Transaction Management", description = "APIs for wallet transactions and transfers")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping
    @Operation(summary = "Apply transaction", description = "Credit or debit a wallet with idempotency support")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Transaction applied successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request or insufficient balance"),
        @ApiResponse(responseCode = "404", description = "Wallet not found")
    })
    public ResponseEntity<ResultWrapper<TransactionResponse>> transact(@Valid @RequestBody TransactionRequest request) {
        TransactionResponse response = transactionService.applyTransaction(request);
        ResultWrapper<TransactionResponse> result = new ResultWrapper<>();
        result.setStatus(ResultWrapper.ResultStatus.SUCCESS);
        result.setMessage("Transaction completed successfully");
        result.setData(response);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/transfer")
    @Operation(summary = "Transfer between wallets", description = "Transfer money between two wallets with idempotency support")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Transfer completed successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request or insufficient balance"),
        @ApiResponse(responseCode = "404", description = "Sender or receiver wallet not found")
    })
    public ResponseEntity<ResultWrapper<TransferResponse>> transfer(@Valid @RequestBody TransferRequest request) {
        TransferResponse response = transactionService.transfer(request);
        ResultWrapper<TransferResponse> result = new ResultWrapper<>();
        result.setStatus(ResultWrapper.ResultStatus.SUCCESS);
        result.setMessage("Transfer completed successfully");
        result.setData(response);
        return ResponseEntity.ok(result);
    }
}
