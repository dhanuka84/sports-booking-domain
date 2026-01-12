package com.sportsbook.payment.validator

import com.sportsbook.payment.contracts.DepositDecision
import com.sportsbook.payment.contracts.DepositEnrichedEvent
import com.sportsbook.payment.contracts.DepositInitiatedEvent
import com.sportsbook.payment.contracts.DepositRejectionReason
import com.sportsbook.payment.contracts.DepositValidatedEvent
import com.sportsbook.payment.contracts.FundingSourceType
import com.sportsbook.payment.validator.domain.DepositDecisionRepository
import com.sportsbook.payment.validator.ports.CreditCheckPort
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.assertj.core.api.Assertions.assertThat
import org.awaitility.Awaitility.await
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.serializer.JsonDeserializer
import org.springframework.kafka.test.EmbeddedKafkaBroker
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.kafka.test.utils.KafkaTestUtils
import org.springframework.test.context.TestPropertySource
import java.math.BigDecimal
import java.time.Duration

@SpringBootTest
@EmbeddedKafka(partitions = 1, topics = ["deposit-enriched", "deposit-validated", "deposit-rejected"])
@TestPropertySource(
    properties = [
        "spring.kafka.bootstrap-servers=\${spring.embedded.kafka.brokers}",
        "spring.datasource.url=jdbc:h2:mem:validator;DB_CLOSE_DELAY=-1",
        "spring.datasource.driverClassName=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.show-sql=false",
    ],
)
class PaymentValidatorIntegrationTest {

    @Autowired
    private lateinit var genericKafkaTemplate: KafkaTemplate<String, Any>

    @Autowired
    private lateinit var broker: EmbeddedKafkaBroker

    @Autowired
    private lateinit var decisionRepository: DepositDecisionRepository

    @MockBean
    private lateinit var creditCheckPort: CreditCheckPort

    @Test
    fun debitDeposit_shouldBeApproved_andPersisted_andEmitValidatedEvent() {
        val initiated = DepositInitiatedEvent.newDeposit(
            "player-int-1",
            BigDecimal("150.00"),
            "SEK",
            "DEBIT",
            "DEBIT-INT-1",
        )
        val enriched = DepositEnrichedEvent.noRisk(initiated)

        `when`(creditCheckPort.checkSource(enriched))
            .thenReturn(FundingSourceType.DEBIT)

        genericKafkaTemplate.send("deposit-enriched", initiated.playerId, enriched)

        await().atMost(Duration.ofSeconds(15))
            .untilAsserted {
                val decisionOpt = decisionRepository.findById(initiated.depositId)
                assertThat(decisionOpt).isPresent
                val decision = decisionOpt.get()
                assertThat(decision.decision).isEqualTo(DepositDecision.APPROVED)
                assertThat(decision.fundingSourceType).isEqualTo(FundingSourceType.DEBIT)
            }

        val consumerProps = KafkaTestUtils.consumerProps("test-valid-consumer", "true", broker)
        consumerProps[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java
        consumerProps[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = JsonDeserializer::class.java
        consumerProps[JsonDeserializer.TRUSTED_PACKAGES] = "com.sportsbook.payment.contracts"

        val cf = DefaultKafkaConsumerFactory<String, DepositValidatedEvent>(consumerProps)
        val consumer = cf.createConsumer()
        broker.consumeFromAnEmbeddedTopic(consumer, "deposit-validated")

        val records = consumer.poll(Duration.ofSeconds(3))

        assertThat(records.count()).isGreaterThanOrEqualTo(1)
    }

    @Test
    fun creditDeposit_shouldBeRejected_andEmitRejectedEvent() {
        val initiated = DepositInitiatedEvent.newDeposit(
            "player-int-2",
            BigDecimal("200.00"),
            "SEK",
            "CARD",
            "CREDIT-INT-1",
        )
        val enriched = DepositEnrichedEvent.noRisk(initiated)

        `when`(creditCheckPort.checkSource(enriched))
            .thenReturn(FundingSourceType.CREDIT)

        genericKafkaTemplate.send("deposit-enriched", initiated.playerId, enriched)

        await().atMost(Duration.ofSeconds(10))
            .untilAsserted {
                val decisionOpt = decisionRepository.findById(initiated.depositId)
                assertThat(decisionOpt).isPresent
                val decision = decisionOpt.get()
                assertThat(decision.decision).isEqualTo(DepositDecision.REJECTED)
                assertThat(decision.rejectionReason).isEqualTo(DepositRejectionReason.CREDIT_CARD)
            }
    }
}
