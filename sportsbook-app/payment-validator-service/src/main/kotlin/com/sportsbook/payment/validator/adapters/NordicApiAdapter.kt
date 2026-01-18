package com.sportsbook.payment.validator.adapters

import com.sportsbook.payment.contracts.DepositEnrichedEvent
import com.sportsbook.payment.contracts.FundingSourceType
import com.sportsbook.payment.validator.ports.CreditCheckPort
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import java.time.Duration

@Service
class NordicApiAdapter(
    builder: WebClient.Builder,
    @Value("\${psv.nordic-api.base-url:http://localhost:8089}") baseUrl: String,
) : CreditCheckPort {

    private val webClient = builder.baseUrl(baseUrl).build()

    @CircuitBreaker(name = "bankApiBreaker")
    override fun checkSource(depositEnriched: DepositEnrichedEvent): FundingSourceType =
        webClient.post()
            .uri("/funding-source/check")
            .bodyValue(depositEnriched)
            .retrieve()
            .bodyToMono(FundingSourceType::class.java)
            .block(Duration.ofSeconds(2)) ?: FundingSourceType.UNKNOWN
}
