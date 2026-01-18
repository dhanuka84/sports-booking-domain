package com.sportsbook.bettingservice.api

import com.sportsbook.bettingservice.api.dto.PlaceBetRequest
import com.sportsbook.bettingservice.domain.Bet
import com.sportsbook.bettingservice.service.BetService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/bets")
class BetController(
    private val bets: BetService,
) {

    @PostMapping
    fun place(@RequestBody req: PlaceBetRequest): ResponseEntity<Bet> {
        val bet = bets.placeBet(req.playerId, req.marketId, req.stake, req.odds)
        return ResponseEntity.accepted().body(bet)
    }
}
