package com.loopers.application.payment.strategy;

import com.loopers.domain.payment.*;
import com.loopers.common.error.CoreException;
import com.loopers.common.error.ErrorType;
import com.loopers.domain.point.Point;
import com.loopers.domain.point.PointRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class PointPaymentAdapter implements PaymentStrategy {

    private final PointRepository pointRepository;
    private final PaymentRepository paymentRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public boolean supports(PaymentMethod method) {
        return method == PaymentMethod.POINT;
    }

    @Override
    @Transactional
    public PayResult requestPayment(PayCommand command) {
        // 포인트 조회할 수 없는 경우는 예외발생
        Point point = pointRepository.findByUserIdForUpdate(command.getUserId())
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "포인트를 조회할 수 없습니다. userId : " + command.getUserId()));

        // 결제 정보 먼저 생성
        Payment payment = Payment.createRequested(command.getUserId(), command.getOrderId(), command.getAmount(), PaymentMethod.POINT);
        try {
            point.deduct(command.getAmount());
            // 성공 케이스
            payment.complete();
            eventPublisher.publishEvent(new PaymentEvent.Completed(payment.getOrderId(), payment.getUserId()));
            return PayResult.success(payment, point);
        } catch (IllegalStateException e) {
            log.warn("Point payment failed: reason=INSUFFICIENT_FUNDS userId={} orderId={} amount={} balance={} msg={}",
                    command.getUserId(), command.getOrderId(), command.getAmount(), point.getBalance(), e.getMessage());
            // 포인트가 부족한 경우 -> 결제 실패 처리
            payment.fail();
            eventPublisher.publishEvent(new PaymentEvent.Failed(payment.getOrderId(), payment.getUserId()));
            return PayResult.fail(PayResult.FailureReason.INSUFFICIENT_FUNDS);
        } finally {
            paymentRepository.save(payment);
        }

    }
}
