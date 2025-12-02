package com.sportsbook.riskservice.streams;

import com.sportsbook.payment.contracts.DepositEnrichedEvent;
import com.sportsbook.payment.contracts.DepositInitiatedEvent;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.kafka.support.serializer.JsonSerde;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;

@Configuration
@EnableKafkaStreams
public class DepositRiskTopology {

    @Bean
    public KStream<String, DepositInitiatedEvent> depositRiskStream(StreamsBuilder builder) {

        Serde<String> stringSerde = Serdes.String();
        JsonSerde<DepositInitiatedEvent> depositSerde =
                new JsonSerde<>(DepositInitiatedEvent.class);
        JsonSerde<DepositEnrichedEvent> enrichedSerde =
                new JsonSerde<>(DepositEnrichedEvent.class);

        KStream<String, DepositInitiatedEvent> deposits =
                builder.stream("deposit-ingest", Consumed.with(stringSerde, depositSerde));

        deposits
                .mapValues(DepositEnrichedEvent::noRisk)
                .to("deposit-enriched", Produced.with(stringSerde, enrichedSerde));

        return deposits;
    }
}
