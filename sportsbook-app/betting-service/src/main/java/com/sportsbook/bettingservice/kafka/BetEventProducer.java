package com.sportsbook.bettingservice.kafka;

import com.sportsbook.bettingservice.domain.Bet;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BetEventProducer {

    private final KafkaTemplate<String, BetPlacedEvent> kafkaTemplate;

    @Value("${app.kafka.topics.bet-placed}")
    private String betPlacedTopic;

    public void publishBetPlaced(BetPlacedEvent event) {
        // use bet id as the key if you want partition affinity
        kafkaTemplate.send(betPlacedTopic, event.getId(), event);
    }
}
