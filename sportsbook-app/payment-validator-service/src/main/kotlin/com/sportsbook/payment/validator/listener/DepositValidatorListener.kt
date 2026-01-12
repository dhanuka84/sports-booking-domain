package com.sportsbook.payment.validator.listener

import com.sportsbook.payment.contracts.DepositEnrichedEvent
import com.sportsbook.payment.validator.service.ValidationOrchestrator
import io.github.resilience4j.circuitbreaker.CallNotPermittedException
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Service

@Service
class DepositValidatorListener(
    private val orchestrator: ValidationOrchestrator,
) {

    @KafkaListener(
        id = "validatorListener",
        topics = ["\${psv.topics.deposit-enriched}"],
        containerFactory = "depositKafkaListenerContainerFactory",
    )
    fun onDeposit(record: ConsumerRecord<String, DepositEnrichedEvent>, ack: Acknowledgment) {
        try {
            orchestrator.validateAndOrchestrate(record.value())
            ack.acknowledge()
        } catch (ex: CallNotPermittedException) {
            throw ex
        } catch (ex: Exception) {
            throw ex
        }
    }
}
