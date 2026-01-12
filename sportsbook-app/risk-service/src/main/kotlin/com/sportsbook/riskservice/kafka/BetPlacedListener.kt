package com.sportsbook.riskservice.kafka

import com.sportsbook.events.BetAccepted
import com.sportsbook.events.BetPlaced
import com.sportsbook.events.BetRejected
import org.apache.kafka.clients.producer.ProducerRecord
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

@Component
class BetPlacedListener(
    private val kafkaTemplate: KafkaTemplate<String, Any>,
) {

    companion object {
        private const val TOPIC_ACCEPTED = "bet.accepted"
        private const val TOPIC_REJECTED = "bet.rejected"
    }

    @KafkaListener(topics = ["bet.placed"], groupId = "risk-service")
    fun onBetPlaced(evt: BetPlaced) {
        val approve = evt.stake <= 5_000

        if (approve) {
            val accepted = BetAccepted.newBuilder()
                .setBetId(evt.betId)
                .setPlayerId(evt.playerId)
                .setMarketId(evt.marketId)
                .setStake(evt.stake)
                .setOdds(evt.odds)
                .build()
            kafkaTemplate.send(ProducerRecord(TOPIC_ACCEPTED, accepted.betId, accepted))
        } else {
            val rejected = BetRejected.newBuilder()
                .setBetId(evt.betId)
                .setReason("Stake too high")
                .build()
            kafkaTemplate.send(ProducerRecord(TOPIC_REJECTED, rejected.betId, rejected))
        }
    }
}
