package com.sportsbook.payment.validator.listener;

import com.sportsbook.payment.contracts.DepositEnrichedEvent;
import com.sportsbook.payment.validator.service.ValidationOrchestrator;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

@Service
public class DepositValidatorListener {

    private final ValidationOrchestrator orchestrator;

    public DepositValidatorListener(ValidationOrchestrator orchestrator) {
        this.orchestrator = orchestrator;
    }

    @KafkaListener(
            id = "validatorListener",
            topics = "${psv.topics.deposit-enriched}",
            containerFactory = "depositKafkaListenerContainerFactory"
    )
    public void onDeposit(ConsumerRecord<String, DepositEnrichedEvent> record, Acknowledgment ack) {
        try {
            orchestrator.validateAndOrchestrate(record.value());
            ack.acknowledge();
        } catch (CallNotPermittedException e) {
            throw e;
        } catch (Exception e) {
            throw e;
        }
    }
}
