package com.sportsbook.bettingservice.messaging;

import com.sportsbook.bettingservice.domain.BetStatus;
import com.sportsbook.bettingservice.repo.BetRepository;
import com.sportsbook.events.BetAccepted;
import com.sportsbook.events.BetRejected;
import com.sportsbook.events.BetSettled;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class BetStatusListener {

  private final BetRepository betRepository;

  @KafkaListener(topics = "bet.accepted", groupId = "betting-service")
  @Transactional
  public void onAccepted(BetAccepted evt) {
    betRepository.findById(evt.getBetId())
        .ifPresent(b -> {
          if (b.getStatus() == BetStatus.PENDING) {
            b.setStatus(BetStatus.ACCEPTED);
            betRepository.save(b);
          }
        });
  }

  @KafkaListener(topics = "bet.rejected", groupId = "betting-service")
  @Transactional
  public void onRejected(BetRejected evt) {
    betRepository.findById(evt.getBetId())
        .ifPresent(b -> {
          b.setStatus(BetStatus.REJECTED);
          betRepository.save(b);
        });
  }

  @KafkaListener(topics = "bet.settled", groupId = "betting-service")
  @Transactional
  public void onSettled(BetSettled evt) {
    betRepository.findById(evt.getBetId())
        .ifPresent(b -> {
          // If BetStatus has no WON/LOST, use SETTLED.
          b.setStatus(BetStatus.SETTLED);
          betRepository.save(b);
        });
  }
}
