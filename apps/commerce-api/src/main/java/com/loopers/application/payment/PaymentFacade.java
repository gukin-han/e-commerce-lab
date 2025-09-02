package com.loopers.application.payment;

import com.loopers.application.payment.dto.InitiateCommand;
import com.loopers.application.payment.dto.SyncPaymentCommand;
import com.loopers.application.payment.strategy.PaymentStrategy;
import com.loopers.application.payment.strategy.PaymentStrategyRouter;
import com.loopers.common.error.CoreException;
import com.loopers.common.error.ErrorType;
import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderService;
import com.loopers.domain.payment.*;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class PaymentFacade {

    // TODO : 어디에 둘지 고민해보기
    public static final String BASE_CALLBACK_URL = "http://localhost:8080/api/v1/payments/%s/callback";
    private final PaymentRepository paymentRepository;
    private final PaymentService paymentService;
    private final PaymentStrategyRouter router;
    private final UserService userService;
    private final OrderService orderService;
    private final PaymentClient paymentClient;
    private final ApplicationEventPublisher eventPublisher;


    //    @Transactional
    public PayResult initiatePayment(String loginId, InitiateCommand command) {
        paymentService.checkDuplicatePayment(command.orderId());
        PayCommand payCommand = this.createPayCommand(loginId, command);
        PaymentStrategy paymentStrategy = router.requestPayment(payCommand.getMethod());
        return paymentStrategy.requestPayment(payCommand);
    }

    private PayCommand createPayCommand(String loginId, InitiateCommand command) {
        User user = userService.getByLoginId(loginId);
        Order order = orderService.get(command.orderId());
        return PayCommand.builder()
                .orderId(order.getId())
                .userId(user.getId())
                .loginId(user.getLoginId())
                .amount(order.getTotalPrice().subtract(order.getDiscountAmount()))
                .method(command.method())
                .callbackUrl(String.format(BASE_CALLBACK_URL, order.getId()))
                .cardType(command.cardType())
                .cardNo(command.cardNo())
                .build();
    }

    @Transactional
    public void syncPaymentCallbacks(Duration window) {
        ZonedDateTime cutoff = ZonedDateTime.now().minus(window);

        int page = 0;
        int size = 100;

        Page<Payment> slice = paymentRepository.findPendingSince(cutoff, PageRequest.of(page, size));
        if (slice.isEmpty()) {
            return;
        }

        slice.forEach(payment -> {
            // 결제 상태 동기화
            List<Transaction> transactions = paymentClient.getTransactionByOrderId(payment.getOrderId());

            if (transactions.isEmpty()) {
                return;
            }

            Transaction transaction = transactions.get(0); // 첫 번째 트랜잭션 사용
            syncPaymentCallback(SyncPaymentCommand.of(payment.getOrderId(), transaction.getStatus(), transaction.getReason()));
        });
    }

    @Transactional
    public void syncPaymentCallback(SyncPaymentCommand command) {
        // 결제 상태 동기화 로직
        // 1. 주문 ID로 결제 정보 조회
        Payment payment = paymentRepository.findByOrderId(command.getOrderId())
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "결제 정보를 찾을 수 없습니다."));

        // 2. 결제 상태 업데이트
        payment.syncStatus(command.getStatus(), command.getReason());

        // 3. 성공/실패에 따라 주문 상태 업데이트 (생략)
        if ("SUCCESS".equals(command.getStatus())) {
            eventPublisher.publishEvent(new PaymentEvent.Completed(payment.getOrderId(), payment.getUserId()));
        }

        if ("FAILED".equals(command.getStatus())) {
            eventPublisher.publishEvent(new PaymentEvent.Failed(payment.getOrderId(), payment.getUserId()));
        }
    }
}
