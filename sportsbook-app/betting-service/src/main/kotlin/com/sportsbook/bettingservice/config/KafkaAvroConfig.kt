package com.sportsbook.bettingservice.config

import com.sportsbook.events.BetPlaced
import io.confluent.kafka.serializers.KafkaAvroSerializer
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory

@Configuration
class KafkaAvroConfig(
    @Value("\${spring.kafka.bootstrap-servers}") private val bootstrap: String,
    @Value("\${spring.kafka.properties.schema.registry.url}") private val schemaRegistryUrl: String,
) {

    @Bean
    fun pf(): ProducerFactory<String, BetPlaced> {
        val props = HashMap<String, Any>()
        props[ProducerConfig.BOOTSTRAP_SERVERS_CONFIG] = bootstrap
        props[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java
        props[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = KafkaAvroSerializer::class.java
        props["schema.registry.url"] = schemaRegistryUrl
        props["auto.register.schemas"] = true
        return DefaultKafkaProducerFactory(props)
    }

    @Bean
    fun kt(): KafkaTemplate<String, BetPlaced> = KafkaTemplate(pf())
}
