package com.sportsbook.payment.contracts.saga

import java.time.Instant

data class WalletCreditedEvent(
    val sagaId: String,
    val depositId: String,
    val playerId: String,
    val success: Boolean,
    val failureReason: String?,
    val occurredAt: Instant,
)
