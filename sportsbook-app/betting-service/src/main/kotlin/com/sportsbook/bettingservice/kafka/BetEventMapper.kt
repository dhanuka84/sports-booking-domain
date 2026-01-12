package com.sportsbook.bettingservice.kafka

import com.sportsbook.bettingservice.domain.Bet
import com.sportsbook.events.BetPlaced
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class BetEventMapper {

    fun toAvro(source: BetPlacedEvent): BetPlaced =
        BetPlaced.newBuilder()
            .setEventId(UUID.randomUUID().toString())
            .setBetId(source.id)
            .setPlayerId(source.playerId)
            .setUserId(source.userId)
            .setMarketId(source.matchId)
            .setOutcomeId(source.selection)
            .setStake(source.stake)
            .setOdds(source.odds)
            .setTimestamp(System.currentTimeMillis())
            .build()

    fun toLocalEvent(bet: Bet): BetPlacedEvent =
        BetPlacedEvent(
            id = bet.id,
            playerId = bet.playerId,
            userId = bet.userId,
            matchId = bet.marketId,
            selection = bet.outcomeId,
            stake = bet.stake,
            odds = bet.odds,
        )
}
