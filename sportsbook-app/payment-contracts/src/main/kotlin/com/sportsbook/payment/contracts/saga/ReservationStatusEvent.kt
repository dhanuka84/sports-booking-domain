package com.sportsbook.payment.contracts.saga

import java.time.Instant

data class ReservationStatusEvent(
    val sagaId: String,
    val depositId: String,
    val success: Boolean,
    val providerReservationId: String?,
    val failureReason: String?,
    val occurredAt: Instant,
)
