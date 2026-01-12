package com.sportsbook.riskservice.streams

import com.sportsbook.payment.contracts.DepositEnrichedEvent
import com.sportsbook.payment.contracts.DepositInitiatedEvent
import org.apache.kafka.common.serialization.Serde
import org.apache.kafka.common.serialization.Serdes
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.kstream.Consumed
import org.apache.kafka.streams.kstream.KStream
import org.apache.kafka.streams.kstream.Produced
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafkaStreams
import org.springframework.kafka.support.serializer.JsonSerde

@Configuration
@EnableKafkaStreams
class DepositRiskTopology {

    @Bean
    fun depositRiskStream(builder: StreamsBuilder): KStream<String, DepositInitiatedEvent> {
        val stringSerde: Serde<String> = Serdes.String()
        val depositSerde = JsonSerde(DepositInitiatedEvent::class.java)
        val enrichedSerde = JsonSerde(DepositEnrichedEvent::class.java)

        val deposits = builder.stream("deposit-ingest", Consumed.with(stringSerde, depositSerde))

        deposits
            .mapValues { DepositEnrichedEvent.noRisk(it) }
            .to("deposit-enriched", Produced.with(stringSerde, enrichedSerde))

        return deposits
    }
}
