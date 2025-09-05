package com.loopers.application.audit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopers.Envelope;
import com.loopers.domain.event.EventLog;
import java.time.Instant;
import lombok.Builder;

public class AuditCommand {

  @Builder
  public record Audit(
      String topic,
      String messageKey,
      String eventType,
      String eventId,
      Instant occurredAt,
      String payload
  ) {

    public static Audit from(Envelope env, ObjectMapper objectMapper) {
      String payloadJson;
      try {
        payloadJson = objectMapper.writeValueAsString(env.payload());
      } catch (JsonProcessingException e) {
        throw new RuntimeException("Failed to serialize payload", e);
      }

      return Audit.builder()
          .topic(env.topic())
          .messageKey(env.key())
          .eventType(env.eventType())
          .eventId(env.eventId())
          .occurredAt(env.occurredAt())
          .payload(payloadJson)
          .build();
    }

    public EventLog toEntity() {
      return new EventLog(this.topic, this.messageKey, this.eventType, this.eventId, this.occurredAt, this.payload);
    }
  }

}
