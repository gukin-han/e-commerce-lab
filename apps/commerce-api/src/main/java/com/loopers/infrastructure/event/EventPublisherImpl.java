package com.loopers.infrastructure.event;

import com.loopers.Envelope;
import com.loopers.KafkaPublisher;
import com.loopers.domain.common.event.EventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class EventPublisherImpl implements EventPublisher {

  private final KafkaPublisher kafkaPublisher;

  @Override
  public void publish(String topic, String key, String eventType, Object payload) {
    Envelope<Object> envelope = Envelope.create(
        topic,
        key,
        eventType,
        payload
    );
    kafkaPublisher.publish(envelope);
  }
}
