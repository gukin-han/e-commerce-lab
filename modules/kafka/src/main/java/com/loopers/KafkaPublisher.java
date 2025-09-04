package com.loopers;

import java.nio.charset.StandardCharsets;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class KafkaPublisher {

  private final KafkaTemplate<String, Object> kafka;

  public KafkaPublisher(KafkaTemplate<String, Object> kafka) {
    this.kafka = kafka;
  }

  public <T> void publish(Envelope<T> envelope) {
    // value에 Envelope<T> 객체를 그대로 넣으면 JsonSerializer가 직렬화
    ProducerRecord<String, Object> record =
        new ProducerRecord<>(envelope.topic(), envelope.key(), envelope);

    // 기본 헤더(원하면 제거 가능)
    record.headers().add("eventId", envelope.eventId().getBytes(StandardCharsets.UTF_8));
    record.headers().add("occurredAt", envelope.occurredAt().toString().getBytes(StandardCharsets.UTF_8));

    kafka.send(record);
  }
}
