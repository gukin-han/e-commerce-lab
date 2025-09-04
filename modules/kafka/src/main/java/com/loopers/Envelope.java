package com.loopers;

import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

public record Envelope<T>(
    String topic,                 // ex) "commerce.order.v1"
    String key,                   // ex) aggregateId / partition key
    String eventId,               // UUID (생성 시 자동 채움 가능)
    Instant occurredAt,           // 생성 시각 (기본값: now)
    T payload,                    // 실제 페이로드
    Map<String, String> headers   // 선택 헤더 (없으면 emptyMap)
) {
  public Envelope {
    if (eventId == null || eventId.isBlank()) eventId = UUID.randomUUID().toString();
    if (occurredAt == null) occurredAt = Instant.now();
    if (headers == null) headers = Collections.emptyMap();
    // topic, key, payload는 호출 측에서 반드시 넣도록 강제 (null 체크는 필요 시 추가)
  }

  // 가장 간단한 팩토리
  public static <T> Envelope<T> create(String topic, String key, T payload) {
    return new Envelope<>(topic, key, null, null, payload, null);
  }

  // 헤더까지 줄 때
  public static <T> Envelope<T> create(String topic, String key, T payload, Map<String, String> headers) {
    return new Envelope<>(topic, key, null, null, payload, headers);
  }
}
