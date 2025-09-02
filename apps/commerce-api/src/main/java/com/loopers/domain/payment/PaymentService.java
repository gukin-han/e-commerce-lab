package com.loopers.domain.payment;

import com.loopers.common.error.CoreException;
import com.loopers.common.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;

    public void checkDuplicatePayment(Long orderId) {
        if (paymentRepository.existsByOrderId(orderId)) {
            throw new CoreException(ErrorType.CONFLICT, "이미 결제 요청이 처리된 주문입니다.");
        }
    }
}
