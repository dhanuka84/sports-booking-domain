package com.sportsbook.payment.contracts

import java.math.BigDecimal
import java.time.Instant

data class DepositValidatedEvent(
    val depositId: String,
    val playerId: String,
    val amount: BigDecimal,
    val fundingSourceType: FundingSourceType,
    val decision: DepositDecision,
    val rejectionReason: DepositRejectionReason?,
    val providerReference: String?,
    val decidedAt: Instant,
)
