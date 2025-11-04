package com.sportsbook.settlementservice.controller;

import com.sportsbook.events.BetSettled;
import com.sportsbook.events.Result;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/results")
@RequiredArgsConstructor
public class SampleController {

  private final KafkaTemplate<String, Object> kafkaTemplate;

  @PostMapping
  public ResponseEntity<String> publish(
      @RequestParam String betId,
      @RequestParam(defaultValue = "WIN") String outcome
  ) {
    BetSettled evt = BetSettled.newBuilder()
        .setBetId(betId)
        .setResult("WIN".equalsIgnoreCase(outcome) ? Result.WIN : Result.LOSE)
        .build();

    kafkaTemplate.send(new ProducerRecord<>("bet.settled", evt.getBetId(), evt));
    return ResponseEntity.accepted().body("OK");
  }
}
