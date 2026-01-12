package com.sportsbook.bettingservice.service

import com.sportsbook.bettingservice.domain.Bet
import com.sportsbook.bettingservice.domain.BetStatus
import com.sportsbook.bettingservice.kafka.BetEventMapper
import com.sportsbook.bettingservice.kafka.BetEventProducer
import com.sportsbook.bettingservice.repo.BetRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class BetService(
    private val betRepository: BetRepository,
    private val producer: BetEventProducer,
    private val eventMapper: BetEventMapper,
) {

    @Transactional
    fun placeBet(playerId: String, marketId: String, stake: Double, odds: Double): Bet {
        val bet = Bet().apply {
            id = UUID.randomUUID().toString()
            this.playerId = playerId
            this.marketId = marketId
            this.stake = stake
            this.stakeCents = stake * 100.0
            this.odds = odds
            this.status = BetStatus.PENDING
        }

        betRepository.save(bet)

        val event = eventMapper.toLocalEvent(bet)
        producer.publishBetPlaced(event)

        return bet
    }
}
