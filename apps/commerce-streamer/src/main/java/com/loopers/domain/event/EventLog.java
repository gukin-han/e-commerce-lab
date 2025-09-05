package com.loopers.domain.event;

import com.loopers.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "event_logs")
@Entity
public class EventLog extends BaseEntity {

  private String topic;
  @Column(name = "message_key")
  private String messageKey;
  private String eventType;
  private String eventId;
  private Instant occurredAt;

  @Column(columnDefinition = "json")
  private String payload;
}
