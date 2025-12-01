package com.sportsbook.payment.validator;

import com.sportsbook.payment.contracts.DepositEnrichedEvent;
import com.sportsbook.payment.contracts.DepositInitiatedEvent;
import com.sportsbook.payment.validator.domain.DepositDecisionRepository;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.test.context.TestPropertySource;
import org.springframework.kafka.test.EmbeddedKafkaBroker;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@EmbeddedKafka(partitions = 1, topics = { "deposit-enriched" })
@TestPropertySource(properties = {
        "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "spring.datasource.url=jdbc:h2:mem:validator_cb;DB_CLOSE_DELAY=-1",
        "spring.datasource.driverClassName=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.show-sql=false"
})
class PaymentValidatorCircuitBreakerTest {

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    @Autowired
    private DepositDecisionRepository decisionRepository;

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @Test
    void whenCircuitBreakerOpen_validatorDoesNotProcessMessages() throws Exception {
        CircuitBreaker cb = circuitBreakerRegistry.circuitBreaker("bankApiBreaker");
        cb.transitionToOpenState();

        long before = decisionRepository.count();

        var initiated = DepositInitiatedEvent.newDeposit(
                "player-cb-1",
                new BigDecimal("99.00"),
                "SEK",
                "CARD",
                "DEBIT-CB-1"
        );
        var enriched = DepositEnrichedEvent.noRisk(initiated);

        KafkaTemplate<String, Object> template = kafkaTemplateForEmbedded();
        template.send("deposit-enriched", initiated.playerId(), enriched).get();

        Thread.sleep(1500);

        long after = decisionRepository.count();

        assertThat(after).isEqualTo(before);
    }

    private KafkaTemplate<String, Object> kafkaTemplateForEmbedded() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, embeddedKafkaBroker.getBrokersAsString());
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        return new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(props));
    }
}
