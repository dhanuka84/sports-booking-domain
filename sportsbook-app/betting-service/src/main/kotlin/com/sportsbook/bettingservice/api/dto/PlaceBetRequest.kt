package com.sportsbook.bettingservice.api.dto

data class PlaceBetRequest(
    val playerId: String,
    val eventId: String,
    val userId: String,
    val marketId: String,
    val outcomeId: String,
    val stake: Double,
    val odds: Double,
)
