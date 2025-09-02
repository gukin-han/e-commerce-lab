package com.loopers.interfaces.event;

import com.loopers.application.order.OrderFacade;
import com.loopers.domain.payment.PaymentEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventListener {

    private final OrderFacade orderFacade;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleCompletedEvent(PaymentEvent.Completed event) {
        try {
            orderFacade.completePayment(event.orderId());
        } catch (Exception e) {
            log.error("Failed to complete Payment: orderId={}", event.orderId(), e);
            throw e;
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleFailedEvent(PaymentEvent.Failed event) {
        try {
            orderFacade.handlePaymentFailure(event.orderId());
        } catch (Exception e) {
            log.error("Failed to handle Payment Failure: orderId={}", event.orderId(), e);
            throw e;
        }
    }
}
