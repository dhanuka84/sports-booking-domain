package com.sportsbook.payment.validator

import com.sportsbook.payment.contracts.DepositEnrichedEvent
import com.sportsbook.payment.contracts.DepositInitiatedEvent
import com.sportsbook.payment.contracts.FundingSourceType
import com.sportsbook.payment.validator.domain.DepositDecisionRepository
import io.github.resilience4j.circuitbreaker.CircuitBreaker
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry
import org.assertj.core.api.Assertions.assertThat
import org.awaitility.Awaitility.await
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.test.context.TestPropertySource
import java.math.BigDecimal
import java.time.Duration

@SpringBootTest
@EmbeddedKafka(partitions = 1, topics = ["deposit-enriched", "deposit-validated", "deposit-rejected"])
@TestPropertySource(
    properties = [
        "spring.kafka.bootstrap-servers=\${spring.embedded.kafka.brokers}",
        "spring.datasource.url=jdbc:h2:mem:validator_bankdown;DB_CLOSE_DELAY=-1",
        "spring.datasource.driverClassName=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.show-sql=false",
        "psv.nordic-api.base-url=http://localhost:18089",
        "resilience4j.circuitbreaker.instances.bankApiBreaker.slidingWindowSize=2",
        "resilience4j.circuitbreaker.instances.bankApiBreaker.minimumNumberOfCalls=2",
        "resilience4j.circuitbreaker.instances.bankApiBreaker.failureRateThreshold=50",
        "resilience4j.circuitbreaker.instances.bankApiBreaker.waitDurationInOpenState=5s",
    ],
)
class PaymentValidatorBankApiDownTest {

    @Autowired
    private lateinit var genericKafkaTemplate: KafkaTemplate<String, Any>

    @Autowired
    private lateinit var depositDecisionRepository: DepositDecisionRepository

    @Autowired
    private lateinit var circuitBreakerRegistry: CircuitBreakerRegistry

    private lateinit var bankApiBreaker: CircuitBreaker

    @BeforeEach
    fun setUp() {
        depositDecisionRepository.deleteAll()
        bankApiBreaker = circuitBreakerRegistry.circuitBreaker("bankApiBreaker")
        bankApiBreaker.reset()
    }

    @AfterEach
    fun tearDown() {
        bankApiBreaker.reset()
    }

    @Test
    fun repeatedBankFailures_shouldOpenCircuitBreaker_andNoDecisionsPersisted() {
        val initiated = DepositInitiatedEvent.newDeposit(
            "player-bank-down-1",
            BigDecimal("50.00"),
            "SEK",
            FundingSourceType.CREDIT.name,
            "DEBIT-BANKDOWN-1",
        )
        val enriched = DepositEnrichedEvent.noRisk(initiated)

        genericKafkaTemplate.send("deposit-enriched", initiated.playerId, enriched)
        genericKafkaTemplate.send("deposit-enriched", initiated.playerId, enriched)

        await()
            .atMost(Duration.ofSeconds(15))
            .untilAsserted {
                assertThat(bankApiBreaker.state).isEqualTo(CircuitBreaker.State.OPEN)
            }

        assertThat(depositDecisionRepository.count()).isZero()
    }
}
