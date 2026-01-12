package com.sportsbook.payment.validator

import com.sportsbook.payment.contracts.DepositEnrichedEvent
import com.sportsbook.payment.contracts.DepositInitiatedEvent
import com.sportsbook.payment.validator.ports.CreditCheckPort
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.assertj.core.api.Assertions.assertThat
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
@EmbeddedKafka(partitions = 1, topics = ["deposit-enriched", "deposit-enriched.DLT"])
@TestPropertySource(
    properties = [
        "spring.kafka.bootstrap-servers=\${spring.embedded.kafka.brokers}",
        "spring.datasource.url=jdbc:h2:mem:validator_dlt;DB_CLOSE_DELAY=-1",
        "spring.datasource.driverClassName=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.show-sql=false",
    ],
)
class PaymentValidatorDltTest {

    @Autowired
    private lateinit var genericKafkaTemplate: KafkaTemplate<String, Any>

    @Autowired
    private lateinit var broker: EmbeddedKafkaBroker

    @MockBean
    private lateinit var creditCheckPort: CreditCheckPort

    @Test
    fun whenProcessingFails_messageIsRoutedToDlt() {
        val initiated = DepositInitiatedEvent.newDeposit(
            "player-dlt-1",
            BigDecimal("50.00"),
            "SEK",
            "CARD",
            "DEBIT-DLT-1",
        )
        val enriched = DepositEnrichedEvent.noRisk(initiated)

        `when`(creditCheckPort.checkSource(enriched))
            .thenThrow(RuntimeException("Bank API hard failure"))

        val consumerProps = KafkaTestUtils.consumerProps("test-dlt-consumer", "true", broker)
        consumerProps[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java
        consumerProps[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = JsonDeserializer::class.java
        consumerProps[JsonDeserializer.TRUSTED_PACKAGES] = "com.sportsbook.payment.contracts"
        consumerProps[JsonDeserializer.VALUE_DEFAULT_TYPE] = DepositEnrichedEvent::class.java
        consumerProps[ConsumerConfig.AUTO_OFFSET_RESET_CONFIG] = "earliest"

        val cf = DefaultKafkaConsumerFactory<String, DepositEnrichedEvent>(consumerProps)
        val consumer = cf.createConsumer()

        broker.consumeFromAnEmbeddedTopic(consumer, "deposit-enriched.DLT")
        genericKafkaTemplate.send("deposit-enriched", initiated.playerId, enriched)

        var totalCount = 0L
        for (i in 0 until 10) {
            val records = consumer.poll(Duration.ofSeconds(1))
            totalCount += records.count()
            if (totalCount >= 1) break
        }

        consumer.close()

        assertThat(totalCount).isGreaterThanOrEqualTo(1)
    }
}
