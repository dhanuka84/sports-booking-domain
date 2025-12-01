package com.sportsbook.payment.validator;

import com.sportsbook.payment.contracts.*;
import com.sportsbook.payment.validator.domain.DepositDecisionRepository;
import com.sportsbook.payment.validator.ports.CreditCheckPort;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@SpringBootTest
@EmbeddedKafka(partitions = 1, topics = { "deposit-enriched", "deposit-validated", "deposit-rejected" })
@TestPropertySource(properties = {
        "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "spring.datasource.url=jdbc:h2:mem:validator;DB_CLOSE_DELAY=-1",
        "spring.datasource.driverClassName=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.show-sql=false"
})
class PaymentValidatorIntegrationTest {

    @Autowired
    private KafkaTemplate<String, Object> genericKafkaTemplate;

    @Autowired
    private EmbeddedKafkaBroker broker;

    @Autowired
    private DepositDecisionRepository decisionRepository;

    @MockBean
    private CreditCheckPort creditCheckPort;

    @Test
    void debitDeposit_shouldBeApproved_andPersisted_andEmitValidatedEvent() {
        var initiated = DepositInitiatedEvent.newDeposit(
                "player-int-1",
                new BigDecimal("150.00"),
                "SEK",
                "CARD",
                "DEBIT-INT-1"
        );
        var enriched = DepositEnrichedEvent.noRisk(initiated);

        when(creditCheckPort.checkSource(enriched))
                .thenReturn(FundingSourceType.DEBIT);

        genericKafkaTemplate.send("deposit-enriched", initiated.playerId(), enriched);

        try {
            Thread.sleep(1500);
        } catch (InterruptedException ignored) {}

        var decisionOpt = decisionRepository.findById(initiated.depositId());
        assertThat(decisionOpt).isPresent();
        var decision = decisionOpt.get();
        assertThat(decision.getDecision()).isEqualTo(DepositDecision.APPROVED);
        assertThat(decision.getFundingSourceType()).isEqualTo(FundingSourceType.DEBIT);

        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps(
                "test-valid-consumer", "true", broker);
        consumerProps.put(org.apache.kafka.clients.consumer.ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class);
        consumerProps.put(org.apache.kafka.clients.consumer.ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                JsonDeserializer.class);
        consumerProps.put(JsonDeserializer.TRUSTED_PACKAGES, "com.sportsbook.payment.contracts");

        var cf = new DefaultKafkaConsumerFactory<String, DepositValidatedEvent>(consumerProps);
        var consumer = cf.createConsumer();
        broker.consumeFromAnEmbeddedTopic(consumer, "deposit-validated");

        ConsumerRecords<String, DepositValidatedEvent> records =
                consumer.poll(Duration.ofSeconds(3));

        assertThat(records.count()).isGreaterThanOrEqualTo(1);
    }

    @Test
    void creditDeposit_shouldBeRejected_andEmitRejectedEvent() {
        var initiated = DepositInitiatedEvent.newDeposit(
                "player-int-2",
                new BigDecimal("200.00"),
                "SEK",
                "CARD",
                "CREDIT-INT-1"
        );
        var enriched = DepositEnrichedEvent.noRisk(initiated);

        when(creditCheckPort.checkSource(enriched))
                .thenReturn(FundingSourceType.CREDIT);

        genericKafkaTemplate.send("deposit-enriched", initiated.playerId(), enriched);

        try {
            Thread.sleep(1500);
        } catch (InterruptedException ignored) {}

        var decisionOpt = decisionRepository.findById(initiated.depositId());
        assertThat(decisionOpt).isPresent();
        var decision = decisionOpt.get();
        assertThat(decision.getDecision()).isEqualTo(DepositDecision.REJECTED);
        assertThat(decision.getRejectionReason()).isEqualTo(DepositRejectionReason.CREDIT_CARD);
    }
}
