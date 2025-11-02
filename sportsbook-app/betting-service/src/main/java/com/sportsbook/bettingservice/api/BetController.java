package com.sportsbook.bettingservice.api;
import com.sportsbook.bettingservice.api.dto.*; import com.sportsbook.bettingservice.service.BetService;
import lombok.RequiredArgsConstructor; import org.springframework.http.ResponseEntity; import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/v1/bets") @RequiredArgsConstructor public class BetController {
  private final BetService bets; @PostMapping public ResponseEntity<PlaceBetResponse> place(@RequestBody PlaceBetRequest req){ return ResponseEntity.accepted().body(new PlaceBetResponse(bets.place(req))); }
}