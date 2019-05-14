package com.tdcr.docker.app.kafka;

import com.tdcr.docker.app.HasLogger;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Configuration
public class KafkaConfiguration implements HasLogger {

    @Value("${kafka.url}")
    String kafkaURL;


    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        getLogger().info("kafka.url: {}",kafkaURL);
        Map<String, Object> config = new HashMap();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaURL);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        return new DefaultKafkaProducerFactory(config);
    }
/*

    private Map<String, Object> consumerConfig() {
        Map<String, Object> config = new HashMap();

        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaURL);
        config.put(ConsumerConfig.GROUP_ID_CONFIG, UUID.randomUUID().toString());
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        return  config;
    }

    public ConsumerFactory<String, String> cosnumerCreateFactory() {
        return new DefaultKafkaConsumerFactory(consumerConfig(),new StringDeserializer(),new JsonDeserializer(String.class));
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String,String> kafkaListenerCreateContainerFactory(){
        ConcurrentKafkaListenerContainerFactory<String,String> factory =
                new ConcurrentKafkaListenerContainerFactory<String, String>();
        factory.setConsumerFactory(cosnumerCreateFactory());
        return factory;
    }
*/


    @Bean
    public KafkaTemplate kafkaTemplate() {
        KafkaTemplate kt = new KafkaTemplate(producerFactory());
        getLogger().info("Kafka initialized..");
        return kt ;
    }

//    @Bean
//    public NewTopic createUserTopic(){
//        return new NewTopic("CreateUserTopic",2,(short)1);
//    }
}
