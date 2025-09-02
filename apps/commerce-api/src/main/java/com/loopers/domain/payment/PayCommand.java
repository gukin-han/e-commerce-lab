package com.loopers.domain.payment;

import com.loopers.domain.order.Order;
import com.loopers.domain.product.Money;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class PayCommand {
    private Long orderId;
    private Long userId;
    private String loginId;
    private Money amount;
    private PaymentMethod method;

    // Optional fields for payment processing
    private String callbackUrl;
    private String cardType;
    private String cardNo;

    public static PayCommand from(Order order, PaymentMethod method) {
        return PayCommand.builder()
                .orderId(order.getId())
                .userId(order.getUserId())
                .amount(order.getTotalPrice())
                .method(method)
                .build();
    }
}
