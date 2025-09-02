package com.loopers.domain.order;

public class OrderEvent {

    public record Created(
            Long orderId,
            Long userID
    ) {
    }

}
