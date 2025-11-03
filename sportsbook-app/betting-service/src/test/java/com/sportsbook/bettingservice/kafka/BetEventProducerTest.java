package com.sportsbook.bettingservice.kafka;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(properties = {
        "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "app.kafka.topics.bet-placed=bets.placed"
})
@EmbeddedKafka(partitions = 1, topics = {"bets.placed"})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class BetEventProducerTest {

    @Autowired
    BetEventProducer producer;

    @Autowired
    EmbeddedKafkaBroker embeddedKafka;

    @Test
    void testKafkaSend() {
        // Arrange
        BetPlacedEvent event = new BetPlacedEvent();
        event.setBetId("test123");
        event.setUserId("u1");
        event.setMatchId("match1");
        event.setSelection("TeamA");
        event.setStake(100);
        event.setOdds(1.75);

        // send
        producer.publishBetPlaced(event);

        // Assert - consume one record and compare
        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps("test-consumer", "true", embeddedKafka);
        JsonDeserializer<BetPlacedEvent> valueDeserializer = new JsonDeserializer<>(BetPlacedEvent.class, false);
        valueDeserializer.addTrustedPackages("*");

        DefaultKafkaConsumerFactory<String, BetPlacedEvent> cf =
                new DefaultKafkaConsumerFactory<>(consumerProps, new StringDeserializer(), valueDeserializer);

        Consumer<String, BetPlacedEvent> consumer = cf.createConsumer();
        embeddedKafka.consumeFromAnEmbeddedTopic(consumer, "bets.placed");
        ConsumerRecord<String, BetPlacedEvent> singleRecord = KafkaTestUtils.getSingleRecord(consumer, "bets.placed", java.time.Duration.ofSeconds(10));

        assertThat(singleRecord.key()).isEqualTo("test123");
        BetPlacedEvent payload = singleRecord.value();
        assertThat(payload.getUserId()).isEqualTo("u1");
        assertThat(payload.getMatchId()).isEqualTo("match1");
        assertThat(payload.getSelection()).isEqualTo("TeamA");
        assertThat(payload.getStake()).isEqualTo(100.0);
        assertThat(payload.getOdds()).isEqualTo(1.75);
        consumer.close();
    }
}