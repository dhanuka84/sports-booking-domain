package com.sportsbook.payment.validator.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.circuitbreaker.event.CircuitBreakerOnStateTransitionEvent;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.stereotype.Component;

@Component
public class KafkaCircuitBreakerConfig {

    private final KafkaListenerEndpointRegistry registry;

    public KafkaCircuitBreakerConfig(
            KafkaListenerEndpointRegistry registry,
            CircuitBreakerRegistry circuitBreakerRegistry) {

        this.registry = registry;

        circuitBreakerRegistry.circuitBreaker("bankApiBreaker")
                .getEventPublisher()
                .onStateTransition(this::handleStateTransition);
    }

    private void handleStateTransition(CircuitBreakerOnStateTransitionEvent event) {
        MessageListenerContainer container = registry.getListenerContainer("validatorListener");
        if (container == null) {
            return;
        }

        switch (event.getStateTransition()) {
            case CLOSED_TO_OPEN, HALF_OPEN_TO_OPEN -> container.pause();
            case OPEN_TO_HALF_OPEN, HALF_OPEN_TO_CLOSED -> container.resume();
        }
    }
}
