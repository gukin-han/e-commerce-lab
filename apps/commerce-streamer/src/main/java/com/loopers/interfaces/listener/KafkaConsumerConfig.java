package com.loopers.interfaces.listener;

import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;

@Configuration
public class KafkaConsumerConfig {

  public ConcurrentKafkaListenerContainerFactory<String, byte[]> kafkaBatchFactory(
      ConsumerFactory<String, byte[]> consumerFactory
  ) {
    ConcurrentKafkaListenerContainerFactory<String, byte[]> factory = new ConcurrentKafkaListenerContainerFactory<>();
    factory.setConsumerFactory(consumerFactory);
    factory.setBatchListener(true);
    factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
    return factory;
  }
}
