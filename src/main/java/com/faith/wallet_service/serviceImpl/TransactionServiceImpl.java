package com.faith.wallet_service.serviceImpl;

import com.faith.wallet_service.dto.TransactionRequest;
import com.faith.wallet_service.dto.TransactionResponse;
import com.faith.wallet_service.dto.TransferRequest;
import com.faith.wallet_service.dto.TransferResponse;
import com.faith.wallet_service.entity.*;
import com.faith.wallet_service.enums.TransactionType;
import com.faith.wallet_service.exception.DuplicateTransactionException;
import com.faith.wallet_service.exception.InsufficientBalanceException;
import com.faith.wallet_service.exception.WalletNotFoundException;
import com.faith.wallet_service.repository.*;
import com.faith.wallet_service.service.TransactionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransactionServiceImpl implements TransactionService {

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final TransferTransactionRepository transferTransactionRepository;
    private final TransactionIdempotencyResponseRepository transactionIdempotencyResponseRepository;
    private final TransferIdempotencyResponseRepository transferIdempotencyResponseRepository;

    public TransactionServiceImpl(WalletRepository walletRepository,
                                 TransactionRepository transactionRepository,
                                 TransferTransactionRepository transferTransactionRepository,
                                 TransactionIdempotencyResponseRepository transactionIdempotencyResponseRepository,
                                 TransferIdempotencyResponseRepository transferIdempotencyResponseRepository) {
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
        this.transferTransactionRepository = transferTransactionRepository;
        this.transactionIdempotencyResponseRepository = transactionIdempotencyResponseRepository;
        this.transferIdempotencyResponseRepository = transferIdempotencyResponseRepository;
    }

    @Override
    @Transactional
    public TransactionResponse applyTransaction(TransactionRequest request) {
        // Check for cached idempotent response first
        var cachedResponse = transactionIdempotencyResponseRepository.findByIdempotencyKey(request.getIdempotencyKey());
        if (cachedResponse.isPresent()) {
            var response = cachedResponse.get();
            return new TransactionResponse(response.getTransactionId(), response.getWalletId(),
                                         response.getBalance(), response.getTimestamp());
        }

        // Check if transaction already exists (shouldn't happen due to unique constraint, but being safe)
        transactionRepository.findByIdempotencyKey(request.getIdempotencyKey())
                .ifPresent(tx -> {
                    throw new DuplicateTransactionException("Transaction already applied");
                });

        Wallet wallet = walletRepository.findById(request.getWalletId())
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found"));

        if (request.getType() == TransactionType.DEBIT) {
            if (wallet.getBalance() < request.getAmount()) {
                throw new InsufficientBalanceException("Insufficient balance");
            }
            wallet.setBalance(wallet.getBalance() - request.getAmount());
        } else {
            wallet.setBalance(wallet.getBalance() + request.getAmount());
        }

        walletRepository.save(wallet);

        WalletTransaction tx = new WalletTransaction();
        tx.setWalletId(wallet.getId());
        tx.setAmount(request.getAmount());
        tx.setType(request.getType());
        tx.setIdempotencyKey(request.getIdempotencyKey());

        tx = transactionRepository.save(tx);

        // Create and cache the response
        TransactionResponse response = new TransactionResponse(tx.getId(), wallet.getId(), wallet.getBalance(), tx.getCreatedAt());
        TransactionIdempotencyResponse idempotencyResponse = new TransactionIdempotencyResponse();
        idempotencyResponse.setIdempotencyKey(request.getIdempotencyKey());
        idempotencyResponse.setTransactionId(tx.getId());
        idempotencyResponse.setWalletId(wallet.getId());
        idempotencyResponse.setBalance(wallet.getBalance());
        idempotencyResponse.setTimestamp(tx.getCreatedAt());

        transactionIdempotencyResponseRepository.save(idempotencyResponse);

        return response;
    }

    @Override
    @Transactional
    public TransferResponse transfer(TransferRequest request) {
        // Check for cached idempotent response first
        var cachedResponse = transferIdempotencyResponseRepository.findByIdempotencyKey(request.getIdempotencyKey());
        if (cachedResponse.isPresent()) {
            var response = cachedResponse.get();
            return new TransferResponse(response.getTransferId(), response.getFromWalletId(), response.getFromBalance(),
                                       response.getToWalletId(), response.getToBalance(), response.getTimestamp());
        }

        Wallet from = walletRepository.findById(request.getFromWalletId())
                .orElseThrow(() -> new WalletNotFoundException("Sender wallet not found"));

        Wallet to = walletRepository.findById(request.getToWalletId())
                .orElseThrow(() -> new WalletNotFoundException("Receiver wallet not found"));

        if (from.getBalance() < request.getAmount()) {
            throw new InsufficientBalanceException("Insufficient balance");
        }

        from.setBalance(from.getBalance() - request.getAmount());
        to.setBalance(to.getBalance() + request.getAmount());

        walletRepository.save(from);
        walletRepository.save(to);

        // Create transfer transaction record
        TransferTransaction transferTx = new TransferTransaction();
        transferTx.setFromWalletId(from.getId());
        transferTx.setToWalletId(to.getId());
        transferTx.setAmount(request.getAmount());
        transferTx.setIdempotencyKey(request.getIdempotencyKey());

        transferTx = transferTransactionRepository.save(transferTx);

        // Create and cache the response
        TransferResponse response = new TransferResponse(transferTx.getId(), from.getId(), from.getBalance(),
                                                        to.getId(), to.getBalance(), transferTx.getCreatedAt());

        TransferIdempotencyResponse idempotencyResponse = new TransferIdempotencyResponse();
        idempotencyResponse.setIdempotencyKey(transferTx.getIdempotencyKey());
        idempotencyResponse.setTransferId(transferTx.getId());
        idempotencyResponse.setFromWalletId(from.getId());
        idempotencyResponse.setFromBalance(from.getBalance());
        idempotencyResponse.setToWalletId(to.getId());
        idempotencyResponse.setToBalance(to.getBalance());
        idempotencyResponse.setTimestamp(transferTx.getCreatedAt());

        transferIdempotencyResponseRepository.save(idempotencyResponse);

        return response;
    }
}
