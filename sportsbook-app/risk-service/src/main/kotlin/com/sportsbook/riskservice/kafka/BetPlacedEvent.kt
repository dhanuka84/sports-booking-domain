package com.sportsbook.riskservice.kafka

data class BetPlacedEvent(
    var betId: String? = null,
    var playerId: String? = null,
    var userId: String? = null,
    var matchId: String? = null,
    var selection: String? = null,
    var stake: Double = 0.0,
    var odds: Double = 0.0,
)
