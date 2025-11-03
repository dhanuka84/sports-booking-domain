package com.sportsbook.bettingservice.api;

import com.sportsbook.bettingservice.api.dto.PlaceBetRequest;
import com.sportsbook.bettingservice.api.dto.PlaceBetResponse;
import com.sportsbook.bettingservice.service.BetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/bets")
@RequiredArgsConstructor
public class BetController {
    private final BetService bets;

    @PostMapping
    public ResponseEntity<PlaceBetResponse> place(@RequestBody PlaceBetRequest req) {
        return ResponseEntity.accepted().body(new PlaceBetResponse(bets.place(req)));
    }
}