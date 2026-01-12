package com.sportsbook.bettingservice.client

import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

@Component
class OddsRestClient {
    private val restTemplate = RestTemplate()

    fun getOdds(matchId: String): Map<String, Double> {
        val raw = restTemplate.getForObject(
            "http://localhost:8083/api/odds/$matchId",
            Map::class.java,
        ) ?: emptyMap<Any, Any>()
        return raw.entries.associate { (key, value) ->
            key.toString() to (value as Number).toDouble()
        }
    }
}
