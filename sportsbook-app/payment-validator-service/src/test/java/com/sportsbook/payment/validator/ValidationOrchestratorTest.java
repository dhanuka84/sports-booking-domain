package com.sportsbook.payment.validator;

import com.sportsbook.payment.contracts.*;
import com.sportsbook.payment.contracts.saga.CreditWalletCommand;
import com.sportsbook.payment.contracts.saga.DepositValidatedSagaEvent;
import com.sportsbook.payment.contracts.saga.ReleaseReservationCommand;
import com.sportsbook.payment.validator.domain.DepositDecisionEntity;
import com.sportsbook.payment.validator.domain.DepositDecisionRepository;
import com.sportsbook.payment.validator.ports.CreditCheckPort;
import com.sportsbook.payment.validator.service.ValidationOrchestrator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class ValidationOrchestratorTest {

    private CreditCheckPort creditCheckPort;
    private DepositDecisionRepository decisionRepository;
    private KafkaTemplate<String, Object> kafkaTemplate;
    private ValidationOrchestrator orchestrator;

    @BeforeEach
    void setUp() {
        creditCheckPort = mock(CreditCheckPort.class);
        decisionRepository = mock(DepositDecisionRepository.class);
        kafkaTemplate = mock(KafkaTemplate.class);
        orchestrator = new ValidationOrchestrator(creditCheckPort, decisionRepository, kafkaTemplate);
    }

    private DepositEnrichedEvent sampleEnriched() {
        var initiated = DepositInitiatedEvent.newDeposit(
                "player-1",
                new BigDecimal("100.00"),
                "SEK",
                "CARD",
                "INSTRUMENT-1"
        );
        return DepositEnrichedEvent.noRisk(initiated);
    }

    @Test
    void debitDeposit_shouldPersistApprovedDecision_andEmitSagaCommands() {
        var enriched = sampleEnriched();
        when(decisionRepository.findById(enriched.original().depositId()))
                .thenReturn(Optional.empty());
        when(creditCheckPort.checkSource(enriched))
                .thenReturn(FundingSourceType.DEBIT);

        orchestrator.validateAndOrchestrate(enriched);

        ArgumentCaptor<DepositDecisionEntity> entityCaptor =
                ArgumentCaptor.forClass(DepositDecisionEntity.class);
        verify(decisionRepository).save(entityCaptor.capture());
        var entity = entityCaptor.getValue();

        assertThat(entity.getDepositId()).isEqualTo(enriched.original().depositId());
        assertThat(entity.getDecision()).isEqualTo(DepositDecision.APPROVED);
        assertThat(entity.getFundingSourceType()).isEqualTo(FundingSourceType.DEBIT);

        verify(kafkaTemplate, atLeastOnce())
                .send(eq("deposit-validated"), eq(enriched.original().playerId()), any(DepositValidatedEvent.class));
        verify(kafkaTemplate, atLeastOnce())
                .send(eq("deposit-events"), anyString(), any(DepositValidatedSagaEvent.class));
        verify(kafkaTemplate, atLeastOnce())
                .send(eq("deposit-commands"), eq(enriched.original().playerId()), any(CreditWalletCommand.class));

        verify(kafkaTemplate, never())
                .send(eq("deposit-commands"), anyString(), any(ReleaseReservationCommand.class));
    }

    @Test
    void creditDeposit_shouldPersistRejectedDecision_andEmitReleaseReservation() {
        var enriched = sampleEnriched();
        when(decisionRepository.findById(enriched.original().depositId()))
                .thenReturn(Optional.empty());
        when(creditCheckPort.checkSource(enriched))
                .thenReturn(FundingSourceType.CREDIT);

        orchestrator.validateAndOrchestrate(enriched);

        ArgumentCaptor<DepositDecisionEntity> entityCaptor =
                ArgumentCaptor.forClass(DepositDecisionEntity.class);
        verify(decisionRepository).save(entityCaptor.capture());
        var entity = entityCaptor.getValue();

        assertThat(entity.getDecision()).isEqualTo(DepositDecision.REJECTED);
        assertThat(entity.getFundingSourceType()).isEqualTo(FundingSourceType.CREDIT);
        assertThat(entity.getRejectionReason()).isEqualTo(DepositRejectionReason.CREDIT_CARD);

        verify(kafkaTemplate, atLeastOnce())
                .send(eq("deposit-rejected"), eq(enriched.original().playerId()), any(DepositValidatedEvent.class));
        verify(kafkaTemplate, atLeastOnce())
                .send(eq("deposit-events"), anyString(), any(DepositValidatedSagaEvent.class));
        verify(kafkaTemplate, atLeastOnce())
                .send(eq("deposit-commands"), eq(enriched.original().depositId()), any(ReleaseReservationCommand.class));

        verify(kafkaTemplate, never())
                .send(eq("deposit-commands"), eq(enriched.original().playerId()), any(CreditWalletCommand.class));
    }

    @Test
    void idempotentBehaviour_shouldSkip_whenDecisionAlreadyExists() {
        var enriched = sampleEnriched();
        var existing = new DepositDecisionEntity();
        existing.setDepositId(enriched.original().depositId());
        existing.setDecidedAt(Instant.now());

        when(decisionRepository.findById(enriched.original().depositId()))
                .thenReturn(Optional.of(existing));

        orchestrator.validateAndOrchestrate(enriched);

        verify(decisionRepository, never()).save(any());
        verifyNoInteractions(kafkaTemplate);
        verifyNoInteractions(creditCheckPort);
    }
}
