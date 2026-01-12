package com.sportsbook.payment.validator.config

import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry
import io.github.resilience4j.circuitbreaker.event.CircuitBreakerOnStateTransitionEvent
import org.springframework.kafka.config.KafkaListenerEndpointRegistry
import org.springframework.kafka.listener.MessageListenerContainer
import org.springframework.stereotype.Component

@Component
class KafkaCircuitBreakerConfig(
    private val registry: KafkaListenerEndpointRegistry,
    circuitBreakerRegistry: CircuitBreakerRegistry,
) {

    init {
        circuitBreakerRegistry.circuitBreaker("bankApiBreaker")
            .eventPublisher
            .onStateTransition(this::handleStateTransition)
    }

    private fun handleStateTransition(event: CircuitBreakerOnStateTransitionEvent) {
        val container: MessageListenerContainer = registry.getListenerContainer("validatorListener") ?: return

        when (event.stateTransition) {
            io.github.resilience4j.circuitbreaker.CircuitBreaker.StateTransition.CLOSED_TO_OPEN,
            io.github.resilience4j.circuitbreaker.CircuitBreaker.StateTransition.HALF_OPEN_TO_OPEN,
            -> container.pause()
            io.github.resilience4j.circuitbreaker.CircuitBreaker.StateTransition.OPEN_TO_HALF_OPEN,
            io.github.resilience4j.circuitbreaker.CircuitBreaker.StateTransition.HALF_OPEN_TO_CLOSED,
            -> container.resume()
            else -> Unit
        }
    }
}
