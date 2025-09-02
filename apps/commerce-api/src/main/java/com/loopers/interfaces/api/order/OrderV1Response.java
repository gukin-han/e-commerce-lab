package com.loopers.interfaces.api.order;

import com.loopers.application.order.OrderResult;

public class OrderV1Response {
    public record Create (){
        public static Create fromResult(OrderResult.Create result) {
            return null;
        }
    }
}
