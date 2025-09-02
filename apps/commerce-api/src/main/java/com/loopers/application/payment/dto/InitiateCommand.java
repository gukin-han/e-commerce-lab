package com.loopers.application.payment.dto;

import com.loopers.domain.payment.PaymentMethod;
import lombok.Builder;

@Builder
public record InitiateCommand(
        Long orderId,
        PaymentMethod method,
        String cardType,
        String cardNo
) {
}
