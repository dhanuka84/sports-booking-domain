package com.sportsbook.bettingservice.kafka

import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

@Component
class BetEventProducer(
    private val kafkaTemplate: KafkaTemplate<String, BetPlacedEvent>,
    @Value("\${app.kafka.topics.bet-placed}") private val betPlacedTopic: String,
) {

    fun publishBetPlaced(event: BetPlacedEvent) {
        val key = requireNotNull(event.id) { "BetPlacedEvent.id is required" }
        kafkaTemplate.send(betPlacedTopic, key, event)
    }
}
