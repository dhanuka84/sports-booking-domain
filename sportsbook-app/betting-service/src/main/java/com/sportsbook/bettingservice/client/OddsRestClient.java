package com.sportsbook.bettingservice.client;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import java.util.Map;

@Component
public class OddsRestClient {
    private final RestTemplate restTemplate = new RestTemplate();

    public Map<String, Double> getOdds(String matchId) {
        return restTemplate.getForObject("http://localhost:8083/api/odds/" + matchId, Map.class);
    }
}
