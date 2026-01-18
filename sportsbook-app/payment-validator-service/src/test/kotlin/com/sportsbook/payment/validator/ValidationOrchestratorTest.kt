package com.sportsbook.payment.validator

import com.sportsbook.payment.contracts.DepositDecision
import com.sportsbook.payment.contracts.DepositEnrichedEvent
import com.sportsbook.payment.contracts.DepositInitiatedEvent
import com.sportsbook.payment.contracts.DepositRejectionReason
import com.sportsbook.payment.contracts.DepositValidatedEvent
import com.sportsbook.payment.contracts.FundingSourceType
import com.sportsbook.payment.contracts.saga.CreditWalletCommand
import com.sportsbook.payment.contracts.saga.DepositValidatedSagaEvent
import com.sportsbook.payment.contracts.saga.ReleaseReservationCommand
import com.sportsbook.payment.validator.domain.DepositDecisionEntity
import com.sportsbook.payment.validator.domain.DepositDecisionRepository
import com.sportsbook.payment.validator.ports.CreditCheckPort
import com.sportsbook.payment.validator.service.ValidationOrchestrator
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.any
import org.mockito.Mockito.anyString
import org.mockito.Mockito.atLeastOnce
import org.mockito.Mockito.eq
import org.mockito.Mockito.mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.Mockito.`when`
import org.springframework.kafka.core.KafkaTemplate
import java.math.BigDecimal
import java.time.Instant
import java.util.Optional

class ValidationOrchestratorTest {

    private lateinit var creditCheckPort: CreditCheckPort
    private lateinit var decisionRepository: DepositDecisionRepository
    private lateinit var kafkaTemplate: KafkaTemplate<String, Any>
    private lateinit var orchestrator: ValidationOrchestrator

    @BeforeEach
    fun setUp() {
        creditCheckPort = mock(CreditCheckPort::class.java)
        decisionRepository = mock(DepositDecisionRepository::class.java)
        @Suppress("UNCHECKED_CAST")
        kafkaTemplate = mock(KafkaTemplate::class.java) as KafkaTemplate<String, Any>
        orchestrator = ValidationOrchestrator(creditCheckPort, decisionRepository, kafkaTemplate)
    }

    private fun sampleEnriched(): DepositEnrichedEvent {
        val initiated = DepositInitiatedEvent.newDeposit(
            "player-1",
            BigDecimal("100.00"),
            "SEK",
            "CARD",
            "INSTRUMENT-1",
        )
        return DepositEnrichedEvent.noRisk(initiated)
    }

    @Test
    fun debitDeposit_shouldPersistApprovedDecision_andEmitSagaCommands() {
        val enriched = sampleEnriched()
        `when`(decisionRepository.findById(enriched.original.depositId))
            .thenReturn(Optional.empty())
        `when`(creditCheckPort.checkSource(enriched))
            .thenReturn(FundingSourceType.DEBIT)

        orchestrator.validateAndOrchestrate(enriched)

        val entityCaptor = ArgumentCaptor.forClass(DepositDecisionEntity::class.java)
        verify(decisionRepository).save(entityCaptor.capture())
        val entity = entityCaptor.value

        assertThat(entity.depositId).isEqualTo(enriched.original.depositId)
        assertThat(entity.decision).isEqualTo(DepositDecision.APPROVED)
        assertThat(entity.fundingSourceType).isEqualTo(FundingSourceType.DEBIT)

        verify(kafkaTemplate, atLeastOnce())
            .send(eq("deposit-validated"), eq(enriched.original.playerId), any(DepositValidatedEvent::class.java))
        verify(kafkaTemplate, atLeastOnce())
            .send(eq("deposit-events"), anyString(), any(DepositValidatedSagaEvent::class.java))
        verify(kafkaTemplate, atLeastOnce())
            .send(eq("deposit-commands"), eq(enriched.original.playerId), any(CreditWalletCommand::class.java))

        verify(kafkaTemplate, never())
            .send(eq("deposit-commands"), anyString(), any(ReleaseReservationCommand::class.java))
    }

    @Test
    fun creditDeposit_shouldPersistRejectedDecision_andEmitReleaseReservation() {
        val enriched = sampleEnriched()
        `when`(decisionRepository.findById(enriched.original.depositId))
            .thenReturn(Optional.empty())
        `when`(creditCheckPort.checkSource(enriched))
            .thenReturn(FundingSourceType.CREDIT)

        orchestrator.validateAndOrchestrate(enriched)

        val entityCaptor = ArgumentCaptor.forClass(DepositDecisionEntity::class.java)
        verify(decisionRepository).save(entityCaptor.capture())
        val entity = entityCaptor.value

        assertThat(entity.decision).isEqualTo(DepositDecision.REJECTED)
        assertThat(entity.fundingSourceType).isEqualTo(FundingSourceType.CREDIT)
        assertThat(entity.rejectionReason).isEqualTo(DepositRejectionReason.CREDIT_CARD)

        verify(kafkaTemplate, atLeastOnce())
            .send(eq("deposit-rejected"), eq(enriched.original.playerId), any(DepositValidatedEvent::class.java))
        verify(kafkaTemplate, atLeastOnce())
            .send(eq("deposit-events"), anyString(), any(DepositValidatedSagaEvent::class.java))
        verify(kafkaTemplate, atLeastOnce())
            .send(eq("deposit-commands"), eq(enriched.original.depositId), any(ReleaseReservationCommand::class.java))

        verify(kafkaTemplate, never())
            .send(eq("deposit-commands"), eq(enriched.original.playerId), any(CreditWalletCommand::class.java))
    }

    @Test
    fun idempotentBehaviour_shouldSkip_whenDecisionAlreadyExists() {
        val enriched = sampleEnriched()
        val existing = DepositDecisionEntity().apply {
            depositId = enriched.original.depositId
            decidedAt = Instant.now()
        }

        `when`(decisionRepository.findById(enriched.original.depositId))
            .thenReturn(Optional.of(existing))

        orchestrator.validateAndOrchestrate(enriched)

        verify(decisionRepository, never()).save(any())
        verifyNoInteractions(kafkaTemplate)
        verifyNoInteractions(creditCheckPort)
    }
}
