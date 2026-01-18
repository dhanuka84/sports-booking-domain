package com.sportsbook.payment.contracts.saga

data class ReleaseReservationCommand(
    val sagaId: String,
    val depositId: String,
    val providerReservationId: String,
)
