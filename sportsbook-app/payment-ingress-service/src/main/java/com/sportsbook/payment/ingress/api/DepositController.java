package com.sportsbook.payment.ingress.api;

import com.sportsbook.payment.contracts.DepositInitiatedEvent;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/deposits")
public class DepositController {

    private final KafkaTemplate<String, DepositInitiatedEvent> kafkaTemplate;
    private final String ingestTopic;

    public DepositController(KafkaTemplate<String, DepositInitiatedEvent> kafkaTemplate,
                             @Value("${psv.topics.deposit-ingest}") String ingestTopic) {
        this.kafkaTemplate = kafkaTemplate;
        this.ingestTopic = ingestTopic;
    }

    public record DepositRequest(
            @NotBlank String playerId,
            @Min(1) BigDecimal amount,
            @NotBlank String currency,
            @NotBlank String paymentMethod,
            @NotBlank String instrumentId
    ) {}

    public record DepositResponse(
            String depositId,
            String status
    ) {}

    @PostMapping
    public DepositResponse createDeposit(@RequestBody @Valid DepositRequest request) {

        var event = DepositInitiatedEvent.newDeposit(
                request.playerId(),
                request.amount(),
                request.currency(),
                request.paymentMethod(),
                request.instrumentId()
        );

        kafkaTemplate.send(ingestTopic, event.playerId(), event);

        return new DepositResponse(event.depositId(), "PENDING");
    }
}
