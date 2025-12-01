package com.sportsbook.walletservice.repository;

import com.sportsbook.walletservice.model.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WalletRepository extends JpaRepository<Wallet, String> {}
