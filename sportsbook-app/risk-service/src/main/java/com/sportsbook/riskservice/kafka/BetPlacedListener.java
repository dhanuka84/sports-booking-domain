package com.sportsbook.riskservice.kafka;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class BetPlacedListener {

    @KafkaListener(topics = "bets.placed", groupId = "risk-service")
    public void handleBetPlaced(BetPlacedEvent event) {
        System.out.println("Risk service received bet: " + event.getBetId());
        // TODO: Update liability tracking
    }
}
