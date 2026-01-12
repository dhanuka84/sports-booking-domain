package com.sportsbook.payment.ingress.api

import com.sportsbook.payment.contracts.DepositInitiatedEvent
import jakarta.validation.Valid
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.math.BigDecimal

@RestController
@RequestMapping("/api/deposits")
class DepositController(
    private val kafkaTemplate: KafkaTemplate<String, DepositInitiatedEvent>,
    @Value("\${psv.topics.deposit-ingest}") private val ingestTopic: String,
) {

    data class DepositRequest(
        @field:NotBlank val playerId: String,
        @field:Min(1) val amount: BigDecimal,
        @field:NotBlank val currency: String,
        @field:NotBlank val paymentMethod: String,
        @field:NotBlank val instrumentId: String,
    )

    data class DepositResponse(
        val depositId: String,
        val status: String,
    )

    @PostMapping
    fun createDeposit(@RequestBody @Valid request: DepositRequest): DepositResponse {
        val event = DepositInitiatedEvent.newDeposit(
            request.playerId,
            request.amount,
            request.currency,
            request.paymentMethod,
            request.instrumentId,
        )

        kafkaTemplate.send(ingestTopic, event.playerId, event)

        return DepositResponse(event.depositId, "PENDING")
    }
}
