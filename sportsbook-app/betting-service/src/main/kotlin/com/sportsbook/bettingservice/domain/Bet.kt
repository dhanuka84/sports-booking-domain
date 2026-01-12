package com.sportsbook.bettingservice.domain

import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "bets")
class Bet {
    @Id
    var id: String? = null
    var playerId: String? = null
    var userId: String? = null
    var eventId: String? = null
    var marketId: String? = null
    var outcomeId: String? = null
    var stake: Double = 0.0
    var odds: Double = 0.0
    var stakeCents: Double = 0.0

    @Enumerated(EnumType.STRING)
    var status: BetStatus? = null
    var createdAt: Instant? = null
    var updatedAt: Instant? = null
}
