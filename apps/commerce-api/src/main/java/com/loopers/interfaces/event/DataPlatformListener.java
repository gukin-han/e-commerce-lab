package com.loopers.interfaces.event;

import com.loopers.domain.order.OrderEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataPlatformListener {

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOrderCreated(OrderEvent.Created message) {

        try {
            log.info("handleOrderCreated: {}", message);
            Thread.sleep(500); // 로그 수집, 이벤트 처리 등
        } catch (InterruptedException e) {
            log.info("handleOrderCreated failed: {}", message, e);
            throw new RuntimeException(e);
        }
    }
}
