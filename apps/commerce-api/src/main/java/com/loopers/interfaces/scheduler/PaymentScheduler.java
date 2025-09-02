package com.loopers.interfaces.scheduler;

import com.loopers.application.payment.PaymentFacade;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentScheduler {

    private final PaymentFacade paymentFacade;

    public static final String EVERY_FIVE_MINUTES = "0 */5 * * * *";

//    @Scheduled(cron = EVERY_FIVE_MINUTES)
    public void syncPayments() {
        log.info("Syncing payments");
        try {
            paymentFacade.syncPaymentCallbacks(Duration.ofHours(1));
            log.info("Payments sync successful");
        } catch (Exception e) {
            log.error("Error during payment synchronization", e);
            // 알림 혹은 재시도 로직
        }
    }
}
