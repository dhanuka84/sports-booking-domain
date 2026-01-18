package com.sportsbook.payment.contracts.saga

import java.math.BigDecimal

data class ReserveFundsCommand(
    val sagaId: String,
    val depositId: String,
    val playerId: String,
    val amount: BigDecimal,
    val currency: String,
    val paymentMethod: String,
    val instrumentId: String,
)
