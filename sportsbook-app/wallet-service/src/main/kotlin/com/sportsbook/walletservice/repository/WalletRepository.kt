package com.sportsbook.walletservice.repository

import com.sportsbook.walletservice.model.Wallet
import org.springframework.data.jpa.repository.JpaRepository

interface WalletRepository : JpaRepository<Wallet, String>
