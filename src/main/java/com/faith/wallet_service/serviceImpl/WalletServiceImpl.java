package com.faith.wallet_service.serviceImpl;

import com.faith.wallet_service.dto.CreateWalletRequest;
import com.faith.wallet_service.entity.Wallet;
import com.faith.wallet_service.exception.WalletNotFoundException;
import com.faith.wallet_service.repository.WalletRepository;
import com.faith.wallet_service.service.WalletService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class WalletServiceImpl implements WalletService {

    private final WalletRepository walletRepository;

    public WalletServiceImpl(WalletRepository walletRepository) {
        this.walletRepository = walletRepository;
    }
// Test
    @Override
    public Wallet createWallet(CreateWalletRequest request) {
        Wallet wallet = new Wallet();
        wallet.setBalance(request.getInitialBalance());
        wallet.setDescription(request.getDescription());
        wallet.setCreatedAt(LocalDateTime.now());
        return walletRepository.save(wallet);
    }

    @Override
    public Wallet getWallet(Long id) {
        return walletRepository.findById(id)
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found with id " + id));
    }
}
