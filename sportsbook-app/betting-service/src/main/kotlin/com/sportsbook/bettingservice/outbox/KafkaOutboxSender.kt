package com.sportsbook.bettingservice.outbox

import com.sportsbook.events.BetPlaced
import org.apache.avro.io.DecoderFactory
import org.apache.avro.specific.SpecificDatumReader
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component
import java.io.ByteArrayInputStream

@Component
class KafkaOutboxSender(
    private val betPlacedTemplate: KafkaTemplate<String, BetPlaced>,
    @Value("\${app.kafka.topics.bet-placed:bets.placed}") private val betPlacedTopic: String,
) : OutboxSendPort {

    override fun send(topic: String, key: String, avroBinary: ByteArray) {
        if (topic == betPlacedTopic) {
            try {
                val reader = SpecificDatumReader<BetPlaced>(BetPlaced.getClassSchema())
                val decoder = DecoderFactory.get().binaryDecoder(ByteArrayInputStream(avroBinary), null)
                val obj = reader.read(null, decoder)
                betPlacedTemplate.send(topic, key, obj)
            } catch (ex: Exception) {
                throw RuntimeException(ex)
            }
        }
    }
}
