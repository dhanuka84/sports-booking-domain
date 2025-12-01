package com.sportsbook.payment.validator.config;

import com.sportsbook.payment.contracts.DepositEnrichedEvent;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public Map<String, Object> consumerConfigs() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "com.sportsbook.payment.contracts");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 5);
        props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, 600_000);
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, 30_000);
        return props;
    }

    @Bean
    public ConsumerFactory<String, DepositEnrichedEvent> depositConsumerFactory() {
        JsonDeserializer<DepositEnrichedEvent> deserializer =
                new JsonDeserializer<>(DepositEnrichedEvent.class);
        deserializer.addTrustedPackages("com.sportsbook.payment.contracts");
        return new DefaultKafkaConsumerFactory<>(consumerConfigs(),
                new StringDeserializer(),
                deserializer);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, DepositEnrichedEvent>
    depositKafkaListenerContainerFactory() {

        var factory = new ConcurrentKafkaListenerContainerFactory<String, DepositEnrichedEvent>();
        factory.setConsumerFactory(depositConsumerFactory());
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);

        var recoverer = new org.springframework.kafka.listener.DeadLetterPublishingRecoverer(
                depositDltKafkaTemplate());
        factory.setCommonErrorHandler(new DefaultErrorHandler(recoverer, new FixedBackOff(0L, 0)));

        return factory;
    }

    @Bean
    public ProducerFactory<String, Object> genericProducerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(org.apache.kafka.clients.producer.ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(org.apache.kafka.clients.producer.ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                org.apache.kafka.common.serialization.StringSerializer.class);
        props.put(org.apache.kafka.clients.producer.ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean
    public KafkaTemplate<String, Object> depositDltKafkaTemplate() {
        return new KafkaTemplate<>(genericProducerFactory());
    }

    @Bean
    public KafkaTemplate<String, Object> depositEventsKafkaTemplate() {
        return new KafkaTemplate<>(genericProducerFactory());
    }
}
