package com.sportsbook.bettingservice.kafka;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class BetEventProducerTest {

    @Autowired
    BetEventProducer producer;

    @Test
    void testKafkaSend() {
        BetPlacedEvent event = new BetPlacedEvent();
        event.setBetId("test123");
        event.setUserId("u1");
        event.setMatchId("match1");
        event.setSelection("TeamA");
        event.setStake(100);
        event.setOdds(1.75);

        producer.publishBetPlaced(event);
    }
}
