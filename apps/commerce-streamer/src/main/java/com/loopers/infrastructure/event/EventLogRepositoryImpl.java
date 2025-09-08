package com.loopers.infrastructure.event;

import com.loopers.domain.event.EventLog;
import com.loopers.domain.event.EventLogRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EventLogRepositoryImpl implements EventLogRepository {

  private final EventLogJpaRepository eventLogJpaRepository;

  @Override
  public void saveAll(List<EventLog> eventLogs) {
    eventLogJpaRepository.saveAll(eventLogs); // 배치 처리는 나중에
  }
}
