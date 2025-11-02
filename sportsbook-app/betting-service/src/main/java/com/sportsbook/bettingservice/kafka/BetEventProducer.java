package com.sportsbook.bettingservice.kafka;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class BetEventProducer {

    private final KafkaTemplate<String, BetPlacedEvent> kafkaTemplate;
    private final String topic;

    public BetEventProducer(
            KafkaTemplate<String, BetPlacedEvent> kafkaTemplate,
            @Value("${app.kafka.topics.bet-placed:bets.placed}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    public void publishBetPlaced(BetPlacedEvent event) {
        kafkaTemplate.send(topic, event.getBetId(), event);
    }
}