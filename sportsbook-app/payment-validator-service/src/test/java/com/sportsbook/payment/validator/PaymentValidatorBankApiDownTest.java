package com.sportsbook.payment.validator;

import com.sportsbook.payment.contracts.DepositEnrichedEvent;
import com.sportsbook.payment.contracts.DepositInitiatedEvent;
import com.sportsbook.payment.contracts.FundingSourceType;
import com.sportsbook.payment.validator.domain.DepositDecisionRepository;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.time.Duration;

import static com.sportsbook.payment.contracts.FundingSourceType.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest
@EmbeddedKafka(partitions = 1, topics = {
        "deposit-enriched",
        "deposit-validated",
        "deposit-rejected"
})
@TestPropertySource(properties = {
        "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "spring.datasource.url=jdbc:h2:mem:validator_bankdown;DB_CLOSE_DELAY=-1",
        "spring.datasource.driverClassName=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.show-sql=false",

        // IMPORTANT: point to a non-listening port so calls fail with ConnectException
        "psv.nordic-api.base-url=http://localhost:18089",

        // Make the breaker open quickly in the test
        "resilience4j.circuitbreaker.instances.bankApiBreaker.slidingWindowSize=2",
        "resilience4j.circuitbreaker.instances.bankApiBreaker.minimumNumberOfCalls=2",
        "resilience4j.circuitbreaker.instances.bankApiBreaker.failureRateThreshold=50",
        "resilience4j.circuitbreaker.instances.bankApiBreaker.waitDurationInOpenState=5s"
})
class PaymentValidatorBankApiDownTest {

    @Autowired
    private KafkaTemplate<String, Object> genericKafkaTemplate;

    @Autowired
    private DepositDecisionRepository depositDecisionRepository;

    @Autowired
    private CircuitBreakerRegistry circuitBreakerRegistry;

    private CircuitBreaker bankApiBreaker;

    @BeforeEach
    void setUp() {
        depositDecisionRepository.deleteAll();
        bankApiBreaker = circuitBreakerRegistry.circuitBreaker("bankApiBreaker");
        bankApiBreaker.reset();
    }

    @AfterEach
    void tearDown() {
        if (bankApiBreaker != null) {
            bankApiBreaker.reset();
        }
    }

    @Test
    void repeatedBankFailures_shouldOpenCircuitBreaker_andNoDecisionsPersisted() {
        // given: a valid enriched deposit that will cause a bank API call
        var initiated = DepositInitiatedEvent.newDeposit(
                "player-bank-down-1",
                new BigDecimal("50.00"),
                "SEK",
                FundingSourceType.CREDIT.name(),
                "DEBIT-BANKDOWN-1"
        );
        var enriched = DepositEnrichedEvent.noRisk(initiated);

        // when: we send multiple messages which will all fail due to bank API being unreachable
        genericKafkaTemplate.send("deposit-enriched", initiated.playerId(), enriched);
        genericKafkaTemplate.send("deposit-enriched", initiated.playerId(), enriched);

        // then: the circuit breaker should eventually open
        await()
                .atMost(Duration.ofSeconds(15))
                .untilAsserted(() ->
                        assertThat(bankApiBreaker.getState())
                                .isEqualTo(CircuitBreaker.State.OPEN)
                );

        // and: no decisions should be persisted when bank API is down
        assertThat(depositDecisionRepository.count()).isZero();
    }
}
