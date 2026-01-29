package com.faith.wallet_service.controller;

import com.faith.wallet_service.dto.CreateWalletRequest;
import com.faith.wallet_service.entity.Wallet;
import com.faith.wallet_service.service.WalletService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/wallets")
@Tag(name = "Wallet Management", description = "APIs for managing wallets")
public class WalletController {

    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    @PostMapping
    @Operation(summary = "Create a new wallet", description = "Creates a new wallet with specified initial balance and optional description")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Wallet created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Wallet> createWallet(@Valid @RequestBody CreateWalletRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(walletService.createWallet(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get wallet details", description = "Retrieves wallet information by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Wallet found"),
        @ApiResponse(responseCode = "404", description = "Wallet not found")
    })
    public ResponseEntity<Wallet> getWallet(@PathVariable Long id) {
        return ResponseEntity.ok(walletService.getWallet(id));
    }
}
