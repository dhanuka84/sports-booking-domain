package com.sportsbook.payment.contracts.saga

import com.sportsbook.payment.contracts.DepositValidatedEvent

data class DepositValidatedSagaEvent(
    val sagaId: String,
    val validation: DepositValidatedEvent,
)
