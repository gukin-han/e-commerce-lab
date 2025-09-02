package com.loopers.interfaces.api.order;

import com.loopers.application.order.OrderCommand;

import java.util.List;

public class OrderV1Request {

    public record Create (Long userId, List<OrderV1Request.CartItem> items, Long couponId) {
        public OrderCommand.Create toCommand() {

            return OrderCommand.Create.of(
                    userId,
                    items.stream().map(CartItem::toCommand).toList(),
                    couponId
            );
        }
    }

    public record CartItem(Long productId, Long quantity) {
        public OrderCommand.CartItem toCommand() {
            return new OrderCommand.CartItem(productId, quantity);
        }
    }
}
