package com.sportsbook.payment.validator.config

import com.sportsbook.payment.contracts.DepositEnrichedEvent
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory
import org.springframework.kafka.listener.ContainerProperties
import org.springframework.kafka.listener.DefaultErrorHandler
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer
import org.springframework.kafka.support.serializer.JsonDeserializer
import org.springframework.kafka.support.serializer.JsonSerializer
import org.springframework.util.backoff.FixedBackOff

@Configuration
class KafkaConfig(
    @Value("\${spring.kafka.bootstrap-servers}")
    private val bootstrapServers: String,
) {

    @Bean
    fun consumerConfigs(): Map<String, Any> {
        val props = HashMap<String, Any>()
        props[ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG] = bootstrapServers
        props[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java
        props[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = JsonDeserializer::class.java
        props[ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG] = false
        props[ConsumerConfig.MAX_POLL_RECORDS_CONFIG] = 5
        props[ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG] = 600_000
        props[ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG] = 30_000
        props[ConsumerConfig.AUTO_OFFSET_RESET_CONFIG] = "earliest"
        return props
    }

    @Bean
    fun depositConsumerFactory(): ConsumerFactory<String, DepositEnrichedEvent> {
        val deserializer = JsonDeserializer(DepositEnrichedEvent::class.java)
        deserializer.addTrustedPackages("com.sportsbook.payment.contracts")
        deserializer.addTrustedPackages("*")
        return DefaultKafkaConsumerFactory(
            consumerConfigs(),
            StringDeserializer(),
            deserializer,
        )
    }

    @Bean
    fun genericProducerFactory(): ProducerFactory<String, Any> {
        val props = HashMap<String, Any>()
        props[ProducerConfig.BOOTSTRAP_SERVERS_CONFIG] = bootstrapServers
        props[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java
        props[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = JsonSerializer::class.java
        return DefaultKafkaProducerFactory(props)
    }

    @Bean
    fun genericKafkaTemplate(): KafkaTemplate<String, Any> =
        KafkaTemplate(genericProducerFactory())

    @Bean
    fun depositKafkaListenerContainerFactory(
        genericKafkaTemplate: KafkaTemplate<String, Any>,
    ): ConcurrentKafkaListenerContainerFactory<String, DepositEnrichedEvent> {
        val factory = ConcurrentKafkaListenerContainerFactory<String, DepositEnrichedEvent>()
        factory.consumerFactory = depositConsumerFactory()
        factory.containerProperties.ackMode = ContainerProperties.AckMode.MANUAL_IMMEDIATE

        val recoverer = DeadLetterPublishingRecoverer(genericKafkaTemplate)

        factory.setCommonErrorHandler(DefaultErrorHandler(recoverer, FixedBackOff(0L, 1L)))

        return factory
    }
}
