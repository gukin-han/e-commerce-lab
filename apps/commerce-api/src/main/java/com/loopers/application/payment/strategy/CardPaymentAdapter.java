package com.loopers.application.payment.strategy;

import com.loopers.domain.payment.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CardPaymentAdapter implements PaymentStrategy {

    private final PaymentClient paymentClient;
    private final PaymentRepository paymentRepository;


    @Override
    public boolean supports(PaymentMethod method) {
        return method == PaymentMethod.CARD;
    }

    @Override
    public PayResult requestPayment(PayCommand command) {
        // 결제 정보 먼저 생성
        try {
            // 결제 요청
            PayResult result = paymentClient.requestPayment(command);
            Payment payment = Payment.createRequested(command.getUserId(), command.getOrderId(), command.getAmount(), PaymentMethod.CARD);
            paymentRepository.save(payment);
            return result;
        } catch (Throwable e) {
            // 에러 발생시 결제 실패 처리
            Payment payment = Payment.createFailed(command.getUserId(), command.getOrderId(), command.getAmount(), PaymentMethod.CARD);
            paymentRepository.save(payment);
            return PayResult.fail(PayResult.FailureReason.NETWORK_ERROR);
        }
    }
}
