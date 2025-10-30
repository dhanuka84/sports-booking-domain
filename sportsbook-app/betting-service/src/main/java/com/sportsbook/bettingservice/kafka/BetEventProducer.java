package com.sportsbook.bettingservice.kafka;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class BetEventProducer {
    private final KafkaTemplate<String, BetPlacedEvent> kafkaTemplate;

    public BetEventProducer(KafkaTemplate<String, BetPlacedEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishBetPlaced(BetPlacedEvent event) {
        kafkaTemplate.send("bets.placed", event.getBetId(), event);
    }
}
