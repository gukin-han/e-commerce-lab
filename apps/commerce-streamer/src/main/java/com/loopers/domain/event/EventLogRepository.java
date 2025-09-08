package com.loopers.domain.event;

import java.util.List;

public interface EventLogRepository {

  void saveAll(List<EventLog> eventLogs);

}
