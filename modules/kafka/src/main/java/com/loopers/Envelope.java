package com.loopers;

import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

public record Envelope<T>(
    String topic,                 // ex) "commerce.order.v1"
    String key,                   // ex) aggregateId / partition key
    String eventType,
    String eventId,               // UUID (생성 시 자동 채움 가능)
    Instant occurredAt,           // 생성 시각 (기본값: now)
    T payload                    // 실제 페이로드
) {
  public Envelope {
    if (eventId == null || eventId.isBlank()) eventId = UUID.randomUUID().toString();
    if (occurredAt == null) occurredAt = Instant.now();
  }

  // 가장 간단한 팩토리
  public static <T> Envelope<T> create(String topic, String key, String eventType, T payload) {
    return new Envelope<>(topic, key, eventType, null, null, payload);
  }

}
