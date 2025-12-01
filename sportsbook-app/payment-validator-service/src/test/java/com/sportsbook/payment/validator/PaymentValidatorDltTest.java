package com.sportsbook.payment.validator;

import com.sportsbook.payment.contracts.DepositEnrichedEvent;
import com.sportsbook.payment.contracts.DepositInitiatedEvent;
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
@EmbeddedKafka(partitions = 1, topics = { "deposit-enriched", "deposit-enriched.DLT" })
@TestPropertySource(properties = {
        "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "spring.datasource.url=jdbc:h2:mem:validator_dlt;DB_CLOSE_DELAY=-1",
        "spring.datasource.driverClassName=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.show-sql=false"
})
class PaymentValidatorDltTest {

    @Autowired
    private KafkaTemplate<String, Object> genericKafkaTemplate;

    @Autowired
    private EmbeddedKafkaBroker broker;

    @MockBean
    private CreditCheckPort creditCheckPort;

    @Test
    void whenProcessingFails_messageIsRoutedToDlt() {
        var initiated = DepositInitiatedEvent.newDeposit(
                "player-dlt-1",
                new BigDecimal("50.00"),
                "SEK",
                "CARD",
                "DEBIT-DLT-1"
        );
        var enriched = DepositEnrichedEvent.noRisk(initiated);

        when(creditCheckPort.checkSource(enriched))
                .thenThrow(new RuntimeException("Bank API hard failure"));


        // --- Set up a dedicated consumer for the DLT topic ---
        Map<String, Object> consumerProps =
                KafkaTestUtils.consumerProps("test-dlt-consumer", "true", broker);

        consumerProps.put(org.apache.kafka.clients.consumer.ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class);
        consumerProps.put(org.apache.kafka.clients.consumer.ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                JsonDeserializer.class);

        // Make sure we can deserialize our contract types
        consumerProps.put(JsonDeserializer.TRUSTED_PACKAGES, "com.sportsbook.payment.contracts");
        consumerProps.put(JsonDeserializer.VALUE_DEFAULT_TYPE, DepositEnrichedEvent.class);
        consumerProps.put(org.apache.kafka.clients.consumer.ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        var cf = new DefaultKafkaConsumerFactory<String, DepositEnrichedEvent>(consumerProps);
        var consumer = cf.createConsumer();

        // subscribe BEFORE sending, so we don't miss early messages
        broker.consumeFromAnEmbeddedTopic(consumer, "deposit-enriched.DLT");
        // Send message that will cause the listener to fail and go to DLT after retries
        genericKafkaTemplate.send("deposit-enriched", initiated.playerId(), enriched);



        long totalCount = 0L;
        // Poll in a loop to give the error handler & DLT publishing time to do their work
        for (int i = 0; i < 10 && totalCount < 1; i++) {
            var records = consumer.poll(Duration.ofSeconds(1));
            totalCount += records.count();
        }

        consumer.close();

        assertThat(totalCount).isGreaterThanOrEqualTo(1);
    }

}
