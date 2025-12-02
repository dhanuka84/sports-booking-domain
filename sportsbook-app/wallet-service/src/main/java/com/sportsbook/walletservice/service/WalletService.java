package com.sportsbook.walletservice.service;

import com.sportsbook.walletservice.model.Wallet;
import com.sportsbook.walletservice.repository.WalletRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class WalletService {

    private final WalletRepository walletRepository;

    public WalletService(WalletRepository walletRepository) {
        this.walletRepository = walletRepository;
    }

    @Transactional
    public void credit(String playerId, BigDecimal amount) {
        Wallet wallet = walletRepository.findById(playerId)
                .orElseGet(() -> new Wallet(playerId, BigDecimal.ZERO));
        wallet.setBalance(wallet.getBalance().add(amount));
        walletRepository.save(wallet);
    }
}
