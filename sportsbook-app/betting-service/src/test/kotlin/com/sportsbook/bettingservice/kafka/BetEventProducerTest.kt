package com.sportsbook.bettingservice.kafka

import com.sportsbook.bettingservice.config.KafkaConfig
import org.apache.kafka.clients.consumer.Consumer
import org.apache.kafka.common.serialization.StringDeserializer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.support.serializer.JsonDeserializer
import org.springframework.kafka.test.EmbeddedKafkaBroker
import org.springframework.kafka.test.context.EmbeddedKafka
import org.springframework.kafka.test.utils.KafkaTestUtils
import org.springframework.test.annotation.DirtiesContext
import java.time.Duration

@SpringBootTest(
    classes = [BetEventProducer::class, KafkaConfig::class, BetEventMapper::class],
    properties = [
        "spring.kafka.bootstrap-servers=\${spring.embedded.kafka.brokers}",
        "app.kafka.topics.bet-placed=bets.placed",
    ],
)
@EmbeddedKafka(partitions = 1, topics = ["bets.placed"])
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class BetEventProducerTest {

    @Autowired
    private lateinit var producer: BetEventProducer

    @Autowired
    private lateinit var embeddedKafka: EmbeddedKafkaBroker

    @Test
    fun testKafkaSend() {
        val event = BetPlacedEvent(
            id = "test123",
            userId = "u1",
            matchId = "match1",
            selection = "TeamA",
            stake = 100.0,
            odds = 1.75,
        )

        producer.publishBetPlaced(event)

        val consumerProps = KafkaTestUtils.consumerProps("test-consumer", "true", embeddedKafka)
        val valueDeserializer = JsonDeserializer(BetPlacedEvent::class.java, false).apply {
            addTrustedPackages("*")
        }
        val cf = DefaultKafkaConsumerFactory(consumerProps, StringDeserializer(), valueDeserializer)

        val consumer: Consumer<String, BetPlacedEvent> = cf.createConsumer()
        embeddedKafka.consumeFromAnEmbeddedTopic(consumer, "bets.placed")
        val singleRecord = KafkaTestUtils.getSingleRecord(consumer, "bets.placed", Duration.ofSeconds(10))

        assertThat(singleRecord.key()).isEqualTo("test123")
        val payload = singleRecord.value()
        assertThat(payload.userId).isEqualTo("u1")
        assertThat(payload.matchId).isEqualTo("match1")
        assertThat(payload.selection).isEqualTo("TeamA")
        assertThat(payload.stake).isEqualTo(100.0)
        assertThat(payload.odds).isEqualTo(1.75)
        consumer.close()
    }
}
