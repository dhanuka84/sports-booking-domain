package com.sportsbook.payment.validator.service;

import com.sportsbook.payment.contracts.*;
import com.sportsbook.payment.contracts.saga.*;
import com.sportsbook.payment.validator.domain.DepositDecisionEntity;
import com.sportsbook.payment.validator.domain.DepositDecisionRepository;
import com.sportsbook.payment.validator.ports.CreditCheckPort;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class ValidationOrchestrator {

    private final CreditCheckPort creditCheckPort;
    private final DepositDecisionRepository decisionRepository;
    private final KafkaTemplate<String, Object> eventsKafkaTemplate;

    private final String validatedTopic = "deposit-validated";
    private final String rejectedTopic = "deposit-rejected";
    private final String commandsTopic = "deposit-commands";
    private final String eventsTopic = "deposit-events";

    public ValidationOrchestrator(CreditCheckPort creditCheckPort,
                                  DepositDecisionRepository decisionRepository,
                                  KafkaTemplate<String, Object> eventsKafkaTemplate) {
        this.creditCheckPort = creditCheckPort;
        this.decisionRepository = decisionRepository;
        this.eventsKafkaTemplate = eventsKafkaTemplate;
    }

    public void validateAndOrchestrate(DepositEnrichedEvent enrichedEvent) {

        var deposit = enrichedEvent.original();

        Optional<DepositDecisionEntity> existing = decisionRepository.findById(deposit.depositId());
        if (existing.isPresent()) {
            return;
        }

        FundingSourceType sourceType = creditCheckPort.checkSource(enrichedEvent);

        DepositDecision decision;
        DepositRejectionReason reason = null;

        switch (sourceType) {
            case DEBIT -> decision = DepositDecision.APPROVED;
            case CREDIT -> {
                decision = DepositDecision.REJECTED;
                reason = DepositRejectionReason.CREDIT_CARD;
            }
            case OVERDRAFT -> {
                decision = DepositDecision.REJECTED;
                reason = DepositRejectionReason.OVERDRAFT;
            }
            case BNPL -> {
                decision = DepositDecision.REJECTED;
                reason = DepositRejectionReason.BNPL_PROVIDER;
            }
            default -> {
                decision = DepositDecision.REJECTED;
                reason = DepositRejectionReason.AMBIGUOUS_BIN;
            }
        }

        var validatedEvent = new DepositValidatedEvent(
                deposit.depositId(),
                deposit.playerId(),
                deposit.amount(),
                sourceType,
                decision,
                reason,
                null,
                Instant.now()
        );

        var entity = new DepositDecisionEntity();
        entity.setDepositId(validatedEvent.depositId());
        entity.setPlayerId(validatedEvent.playerId());
        entity.setAmount(validatedEvent.amount());
        entity.setFundingSourceType(validatedEvent.fundingSourceType());
        entity.setDecision(validatedEvent.decision());
        entity.setRejectionReason(validatedEvent.rejectionReason());
        entity.setProviderReference(validatedEvent.providerReference());
        entity.setDecidedAt(validatedEvent.decidedAt());
        decisionRepository.save(entity);

        if (decision == DepositDecision.APPROVED) {
            eventsKafkaTemplate.send(validatedTopic, deposit.playerId(), validatedEvent);

            String sagaId = UUID.randomUUID().toString();
            var sagaEvent = new DepositValidatedSagaEvent(sagaId, validatedEvent);
            eventsKafkaTemplate.send(eventsTopic, validatedEvent.depositId(), sagaEvent);

            var creditWalletCommand = new CreditWalletCommand(
                    sagaId,
                    validatedEvent.depositId(),
                    validatedEvent.playerId(),
                    validatedEvent.amount()
            );
            eventsKafkaTemplate.send(commandsTopic, validatedEvent.playerId(), creditWalletCommand);

        } else {
            eventsKafkaTemplate.send(rejectedTopic, deposit.playerId(), validatedEvent);

            String sagaId = UUID.randomUUID().toString();
            var sagaEvent = new DepositValidatedSagaEvent(sagaId, validatedEvent);
            eventsKafkaTemplate.send(eventsTopic, validatedEvent.depositId(), sagaEvent);

            var releaseCommand = new ReleaseReservationCommand(
                    sagaId,
                    validatedEvent.depositId(),
                    "psp-reservation-id-placeholder"
            );
            eventsKafkaTemplate.send(commandsTopic, validatedEvent.depositId(), releaseCommand);
        }
    }
}
