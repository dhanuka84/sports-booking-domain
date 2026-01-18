package com.sportsbook.walletservice.service

import com.sportsbook.walletservice.model.Wallet
import com.sportsbook.walletservice.repository.WalletRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

@Service
class WalletService(
    private val walletRepository: WalletRepository,
) {

    @Transactional
    fun credit(playerId: String, amount: BigDecimal) {
        val wallet = walletRepository.findById(playerId)
            .orElseGet { Wallet(playerId, BigDecimal.ZERO) }
        wallet.balance = wallet.balance.add(amount)
        walletRepository.save(wallet)
    }
}
