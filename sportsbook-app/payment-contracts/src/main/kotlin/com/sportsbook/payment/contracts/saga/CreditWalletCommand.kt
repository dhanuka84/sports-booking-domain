package com.sportsbook.payment.contracts.saga

import java.math.BigDecimal

data class CreditWalletCommand(
    val sagaId: String,
    val depositId: String,
    val playerId: String,
    val amount: BigDecimal,
)
