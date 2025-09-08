package com.loopers.application.audit;

import com.loopers.application.audit.AuditCommand.Audit;
import com.loopers.domain.event.EventLog;
import com.loopers.domain.event.EventLogRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class AuditFacade {

  private final EventLogRepository eventLogRepository;

  @Transactional
  public void appendAll(List<Audit> chunk) {

    List<EventLog> eventLogs = chunk.stream()
        .map(Audit::toEntity).toList();
    eventLogRepository.saveAll(eventLogs);
  }
}
