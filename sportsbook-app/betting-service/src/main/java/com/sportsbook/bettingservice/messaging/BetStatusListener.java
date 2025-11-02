package com.sportsbook.bettingservice.messaging;
import com.sportsbook.bettingservice.domain.*; import com.sportsbook.bettingservice.repo.BetRepository; import com.sportsbook.events.*; import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener; import org.springframework.messaging.simp.SimpMessagingTemplate; import org.springframework.stereotype.Component; import java.time.Instant;
@Component @RequiredArgsConstructor public class BetStatusListener {
  private final BetRepository repo; private final SimpMessagingTemplate ws;
  @KafkaListener(topics="${app.kafka.topics.bet-accepted:bets.accepted}", groupId="betting-service")
  public void onAccepted(BetAccepted e){ repo.findById(e.getBetId()).ifPresent(b->{ b.setStatus(BetStatus.ACCEPTED).setUpdatedAt(Instant.now()); repo.save(b); ws.convertAndSend("/topic/bets/"+b.getBetId(),"ACCEPTED");}); }
  @KafkaListener(topics="${app.kafka.topics.bet-rejected:bets.rejected}", groupId="betting-service")
  public void onRejected(BetRejected e){ repo.findById(e.getBetId()).ifPresent(b->{ b.setStatus(BetStatus.REJECTED).setUpdatedAt(Instant.now()); repo.save(b); ws.convertAndSend("/topic/bets/"+b.getBetId(),"REJECTED:"+e.getReason());}); }
  @KafkaListener(topics="${app.kafka.topics.bet-settled:bets.settled}", groupId="betting-service")
  public void onSettled(BetSettled e){ repo.findById(e.getBetId()).ifPresent(b->{ b.setStatus(BetStatus.SETTLED).setUpdatedAt(Instant.now()); repo.save(b); ws.convertAndSend("/topic/bets/"+b.getBetId(),"SETTLED:"+e.getResult());}); }
}