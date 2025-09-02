package com.loopers.domain.payment;

public class PaymentEvent {

    public record Completed(
            Long orderId,
            Long userId
    ) {
    }

    public record Failed(
            Long orderId,
            Long userId
    ) {
    }
}
