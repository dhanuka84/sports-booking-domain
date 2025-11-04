package com.sportsbook.riskservice.kafka;

import com.sportsbook.events.BetAccepted;
import com.sportsbook.events.BetPlaced;
import com.sportsbook.events.BetRejected;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BetPlacedListener {

  private static final String TOPIC_ACCEPTED = "bet.accepted";
  private static final String TOPIC_REJECTED = "bet.rejected";

  private final KafkaTemplate<String, Object> kafkaTemplate;

  @KafkaListener(topics = "bet.placed", groupId = "risk-service")
  public void onBetPlaced(BetPlaced evt) {
    boolean approve = evt.getStake() <= 5_000; // <= $50.00

    if (approve) {
      BetAccepted accepted = BetAccepted.newBuilder()
          .setBetId(evt.getBetId())
          .setPlayerId(evt.getPlayerId())
          .setMarketId(evt.getMarketId())
          .setStake(evt.getStake())
          .setOdds(evt.getOdds())
          .build();
      kafkaTemplate.send(new ProducerRecord<>(TOPIC_ACCEPTED, accepted.getBetId(), accepted));
    } else {
      BetRejected rejected = BetRejected.newBuilder()
          .setBetId(evt.getBetId())
          .setReason("Stake too high")
          .build();
      kafkaTemplate.send(new ProducerRecord<>(TOPIC_REJECTED, rejected.getBetId(), rejected));
    }
  }
}
