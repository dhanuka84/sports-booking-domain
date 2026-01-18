package com.sportsbook.oddsservice.rest

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/odds")
class OddsMarketController {

    @GetMapping("/markets")
    fun getMarkets(): List<MarketDto> {
        return listOf(
            MarketDto(
                id = "market-1",
                sport = "football",
                name = "Match Winner",
            ),
        )
    }
}

data class MarketDto(
    val id: String,
    val sport: String,
    val name: String,
)
