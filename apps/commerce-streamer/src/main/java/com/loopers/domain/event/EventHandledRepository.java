package com.loopers.domain.event;

import org.springframework.data.jpa.repository.JpaRepository;

public interface EventHandledRepository {

  boolean insertIgnoreInto(String eventId);
}