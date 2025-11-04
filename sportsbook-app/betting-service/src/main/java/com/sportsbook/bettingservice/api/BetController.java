package com.sportsbook.bettingservice.api;

import com.sportsbook.bettingservice.api.dto.PlaceBetRequest;
import com.sportsbook.bettingservice.domain.Bet;
import com.sportsbook.bettingservice.service.BetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bets")
@RequiredArgsConstructor
public class BetController {

  private final BetService bets;

  @PostMapping
  public ResponseEntity<Bet> place(@RequestBody PlaceBetRequest req) {
    Bet b = bets.placeBet(req.playerId(), req.marketId(), req.stake(), req.odds());
    return ResponseEntity.accepted().body(b);
  }
}
