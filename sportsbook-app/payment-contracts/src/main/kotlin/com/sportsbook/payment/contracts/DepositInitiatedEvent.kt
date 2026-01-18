package com.sportsbook.payment.contracts

import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

data class DepositInitiatedEvent(
    val depositId: String,
    val playerId: String,
    val amount: BigDecimal,
    val currency: String,
    val paymentMethod: String,
    val instrumentId: String,
    val createdAt: Instant,
) {
    companion object {
        @JvmStatic
        fun newDeposit(
            playerId: String,
            amount: BigDecimal,
            currency: String,
            paymentMethod: String,
            instrumentId: String,
        ): DepositInitiatedEvent =
            DepositInitiatedEvent(
                UUID.randomUUID().toString(),
                playerId,
                amount,
                currency,
                paymentMethod,
                instrumentId,
                Instant.now(),
            )
    }
}
