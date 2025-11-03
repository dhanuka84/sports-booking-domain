package com.sportsbook.bettingservice.outbox;

import com.sportsbook.events.BetPlaced;
import lombok.RequiredArgsConstructor;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;

@Component
@RequiredArgsConstructor
public class KafkaOutboxSender implements OutboxSendPort {
    private final KafkaTemplate<String, BetPlaced> betPlacedTemplate;
    @Value("${app.kafka.topics.bet-placed:bets.placed}")
    private String betPlacedTopic;

    public void send(String topic, String key, byte[] avro) {
        if (topic.equals(betPlacedTopic)) {
            try {
                var reader = new SpecificDatumReader<BetPlaced>(BetPlaced.getClassSchema());
                var dec = DecoderFactory.get().binaryDecoder(new ByteArrayInputStream(avro), null);
                BetPlaced obj = reader.read(null, dec);
                betPlacedTemplate.send(topic, key, obj);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}