package com.loopers.infrastructure.event;

import com.loopers.domain.common.event.EventPublisher;
import com.loopers.domain.common.event.OutBoundEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class AfterCommitEventRelay {
  private final EventPublisher eventPublisher;

  @Async
  @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
  public void on(OutBoundEvent e) {
    eventPublisher.publish(e.topic(), e.key(), e.eventType(), e.payload());
  }
}
