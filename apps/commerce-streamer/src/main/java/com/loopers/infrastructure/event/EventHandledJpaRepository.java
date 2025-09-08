package com.loopers.infrastructure.event;

import com.loopers.domain.event.EventHandled;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface EventHandledJpaRepository extends JpaRepository<EventHandled, Long> {

  boolean existsByEventId(String eventId);

  @Modifying(clearAutomatically = true, flushAutomatically = true)
  @Query(value = """
    INSERT IGNORE INTO event_handled (event_id)
    VALUES (:eventId)
    """, nativeQuery = true)
  int insertIgnoreInto(@Param("eventId") String eventId);
}
