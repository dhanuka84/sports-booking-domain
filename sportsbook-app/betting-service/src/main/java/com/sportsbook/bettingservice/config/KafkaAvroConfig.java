package com.sportsbook.bettingservice.config;
import com.sportsbook.events.BetPlaced; import io.confluent.kafka.serializers.KafkaAvroSerializer;
import org.apache.kafka.clients.producer.ProducerConfig; import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value; import org.springframework.context.annotation.*; import org.springframework.kafka.core.*; import java.util.*;
@Configuration public class KafkaAvroConfig {
  @Value("${spring.kafka.bootstrap-servers}") private String bootstrap; @Value("${spring.kafka.properties.schema.registry.url}") private String sr;
  @Bean public ProducerFactory<String,BetPlaced> pf(){ Map<String,Object> p=new HashMap<>(); p.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,bootstrap);
    p.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,StringSerializer.class); p.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,KafkaAvroSerializer.class);
    p.put("schema.registry.url",sr); p.put("auto.register.schemas",true); return new DefaultKafkaProducerFactory<>(p); }
  @Bean public KafkaTemplate<String,BetPlaced> kt(){ return new KafkaTemplate<>(pf()); }
}