package com.sportsbook.payment.validator.service

import com.sportsbook.payment.contracts.DepositDecision
import com.sportsbook.payment.contracts.DepositEnrichedEvent
import com.sportsbook.payment.contracts.DepositRejectionReason
import com.sportsbook.payment.contracts.DepositValidatedEvent
import com.sportsbook.payment.contracts.FundingSourceType
import com.sportsbook.payment.contracts.saga.CreditWalletCommand
import com.sportsbook.payment.contracts.saga.DepositValidatedSagaEvent
import com.sportsbook.payment.contracts.saga.ReleaseReservationCommand
import com.sportsbook.payment.validator.domain.DepositDecisionEntity
import com.sportsbook.payment.validator.domain.DepositDecisionRepository
import com.sportsbook.payment.validator.ports.CreditCheckPort
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.UUID

@Service
class ValidationOrchestrator(
    private val creditCheckPort: CreditCheckPort,
    private val decisionRepository: DepositDecisionRepository,
    private val eventsKafkaTemplate: KafkaTemplate<String, Any>,
) {

    private val validatedTopic = "deposit-validated"
    private val rejectedTopic = "deposit-rejected"
    private val commandsTopic = "deposit-commands"
    private val eventsTopic = "deposit-events"

    fun validateAndOrchestrate(enrichedEvent: DepositEnrichedEvent) {
        val deposit = enrichedEvent.original

        if (decisionRepository.findById(deposit.depositId).isPresent) {
            return
        }

        val sourceType = creditCheckPort.checkSource(enrichedEvent)

        val decision: DepositDecision
        var reason: DepositRejectionReason? = null

        when (sourceType) {
            FundingSourceType.DEBIT -> decision = DepositDecision.APPROVED
            FundingSourceType.CREDIT -> {
                decision = DepositDecision.REJECTED
                reason = DepositRejectionReason.CREDIT_CARD
            }
            FundingSourceType.OVERDRAFT -> {
                decision = DepositDecision.REJECTED
                reason = DepositRejectionReason.OVERDRAFT
            }
            FundingSourceType.BNPL -> {
                decision = DepositDecision.REJECTED
                reason = DepositRejectionReason.BNPL_PROVIDER
            }
            else -> {
                decision = DepositDecision.REJECTED
                reason = DepositRejectionReason.AMBIGUOUS_BIN
            }
        }

        val validatedEvent = DepositValidatedEvent(
            deposit.depositId,
            deposit.playerId,
            deposit.amount,
            sourceType,
            decision,
            reason,
            null,
            Instant.now(),
        )

        val entity = DepositDecisionEntity().apply {
            depositId = validatedEvent.depositId
            playerId = validatedEvent.playerId
            amount = validatedEvent.amount
            fundingSourceType = validatedEvent.fundingSourceType
            this.decision = validatedEvent.decision
            rejectionReason = validatedEvent.rejectionReason
            providerReference = validatedEvent.providerReference
            decidedAt = validatedEvent.decidedAt
        }
        decisionRepository.save(entity)

        if (decision == DepositDecision.APPROVED) {
            eventsKafkaTemplate.send(validatedTopic, deposit.playerId, validatedEvent)

            val sagaId = UUID.randomUUID().toString()
            val sagaEvent = DepositValidatedSagaEvent(sagaId, validatedEvent)
            eventsKafkaTemplate.send(eventsTopic, validatedEvent.depositId, sagaEvent)

            val creditWalletCommand = CreditWalletCommand(
                sagaId,
                validatedEvent.depositId,
                validatedEvent.playerId,
                validatedEvent.amount,
            )
            eventsKafkaTemplate.send(commandsTopic, validatedEvent.playerId, creditWalletCommand)
        } else {
            eventsKafkaTemplate.send(rejectedTopic, deposit.playerId, validatedEvent)

            val sagaId = UUID.randomUUID().toString()
            val sagaEvent = DepositValidatedSagaEvent(sagaId, validatedEvent)
            eventsKafkaTemplate.send(eventsTopic, validatedEvent.depositId, sagaEvent)

            val releaseCommand = ReleaseReservationCommand(
                sagaId,
                validatedEvent.depositId,
                "psp-reservation-id-placeholder",
            )
            eventsKafkaTemplate.send(commandsTopic, validatedEvent.depositId, releaseCommand)
        }
    }
}
