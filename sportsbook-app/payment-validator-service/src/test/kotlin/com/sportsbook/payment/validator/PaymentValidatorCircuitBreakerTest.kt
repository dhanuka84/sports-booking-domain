package com.sportsbook.payment.validator

import com.sportsbook.payment.contracts.DepositEnrichedEvent
import com.sportsbook.payment.contracts.DepositInitiatedEvent
import com.sportsbook.payment.validator.domain.DepositDecisionRepository
import io.github.resilience4j.circuitbreaker.CircuitBreaker
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringSerializer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.serializer.JsonSerializer
import org.springframework.kafka.test.EmbeddedKafkaBroker
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.test.context.TestPropertySource
import java.math.BigDecimal

@SpringBootTest
@EmbeddedKafka(partitions = 1, topics = ["deposit-enriched"])
@TestPropertySource(
    properties = [
        "spring.kafka.bootstrap-servers=\${spring.embedded.kafka.brokers}",
        "spring.datasource.url=jdbc:h2:mem:validator_cb;DB_CLOSE_DELAY=-1",
        "spring.datasource.driverClassName=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.show-sql=false",
    ],
)
class PaymentValidatorCircuitBreakerTest {

    @Autowired
    private lateinit var circuitBreakerRegistry: CircuitBreakerRegistry

    @Autowired
    private lateinit var decisionRepository: DepositDecisionRepository

    @Autowired
    private lateinit var embeddedKafkaBroker: EmbeddedKafkaBroker

    @Test
    fun whenCircuitBreakerOpen_validatorDoesNotProcessMessages() {
        val cb = circuitBreakerRegistry.circuitBreaker("bankApiBreaker")
        cb.transitionToOpenState()

        val before = decisionRepository.count()

        val initiated = DepositInitiatedEvent.newDeposit(
            "player-cb-1",
            BigDecimal("99.00"),
            "SEK",
            "CARD",
            "DEBIT-CB-1",
        )
        val enriched = DepositEnrichedEvent.noRisk(initiated)

        val template = kafkaTemplateForEmbedded()
        template.send("deposit-enriched", initiated.playerId, enriched).get()

        Thread.sleep(1500)

        val after = decisionRepository.count()

        assertThat(after).isEqualTo(before)
    }

    private fun kafkaTemplateForEmbedded(): KafkaTemplate<String, Any> {
        val props = HashMap<String, Any>()
        props[ProducerConfig.BOOTSTRAP_SERVERS_CONFIG] = embeddedKafkaBroker.brokersAsString
        props[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java
        props[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = JsonSerializer::class.java

        return KafkaTemplate(DefaultKafkaProducerFactory(props))
    }
}
