package com.faith.wallet_service.service;

import com.faith.wallet_service.dto.CreateWalletRequest;
import com.faith.wallet_service.entity.Wallet;

public interface WalletService {
    Wallet createWallet(CreateWalletRequest request);
    Wallet getWallet(Long id);
}
