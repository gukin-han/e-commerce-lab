package com.loopers.application.order;

public class OrderResult {

    public record Create(Long orderId, Status status, String message) {

        public static OrderResult.Create success(Long orderId) {
            return new OrderResult.Create(orderId, Status.SUCCESS, null);
        }
    }

    enum Status {
        FAIL, SUCCESS,
    }
}

