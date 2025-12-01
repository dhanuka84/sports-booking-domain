package com.sportsbook.payment.validator.adapters;

import com.sportsbook.payment.contracts.DepositEnrichedEvent;
import com.sportsbook.payment.contracts.FundingSourceType;
import com.sportsbook.payment.validator.ports.CreditCheckPort;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;

@Service
public class NordicApiAdapter implements CreditCheckPort {

    private final WebClient webClient;

    public NordicApiAdapter(WebClient.Builder builder,
                            @Value("${psv.nordic-api.base-url:http://localhost:8089}") String baseUrl) {
        this.webClient = builder.baseUrl(baseUrl).build();
    }

    @Override
    @CircuitBreaker(name = "bankApiBreaker")
    public FundingSourceType checkSource(DepositEnrichedEvent depositEnriched) {
        // Placeholder API call; replace with real Open Banking integration
        return webClient.post()
                .uri("/funding-source/check")
                .bodyValue(depositEnriched)
                .retrieve()
                .bodyToMono(FundingSourceType.class)
                .block(Duration.ofSeconds(3));
    }
}
