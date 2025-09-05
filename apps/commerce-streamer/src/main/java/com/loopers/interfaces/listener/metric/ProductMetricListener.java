package com.loopers.interfaces.listener.metric;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopers.Envelope;
import com.loopers.application.metric.ProductMetricCommand.Change;
import com.loopers.application.metric.ProductMetricFacade;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.apache.kafka.clients.consumer.ConsumerRecord;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductMetricListener {

  public static final String LIKE_ADDED_V1 = "like.added.v1";
  public static final String LIKE_DELETED_V1 = "like.deleted.v1";
  private final ObjectMapper objectMapper;
  private final ProductMetricFacade productMetricFacade;

  @KafkaListener(
      topics = "like-events",
      groupId = "product-metric-group"
  )
  public void handleLikeChangedEvent(ConsumerRecord<String, String> record) {
    try {
      // 1. Envelope
      Envelope<ProductMetricEvent.Like> envelope = objectMapper.readValue(record.value(), new TypeReference<>() {});

      // 2. payload를 구체 타입으로 역직렬화
      String eventType = envelope.eventType();
      Change command = new Change(envelope.payload().productId(), envelope.eventId(), LocalDate.now());
      if (LIKE_ADDED_V1.equals(eventType)) {
        productMetricFacade.increaseLikeCount(command);
      } else if (LIKE_DELETED_V1.equals(eventType)) {
        productMetricFacade.decreaseLikeCount(command);
      }

    } catch (Exception e) {
      log.error("Failed to parse event payload: {}", record.value(), e);
    }
  }
}
