package com.sportsbook.bettingservice.kafka

data class BetPlacedEvent(
    var id: String? = null,
    var playerId: String? = null,
    var matchId: String? = null,
    var userId: String? = null,
    var selection: String? = null,
    var stake: Double = 0.0,
    var odds: Double = 0.0,
)
