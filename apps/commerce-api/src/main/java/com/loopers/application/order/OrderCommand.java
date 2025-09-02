package com.loopers.application.order;

import lombok.Data;

import java.util.List;

@Data
public class OrderCommand {

    public record Create(Long userId, List<OrderCommand.CartItem> items, Long couponId) {

        public static OrderCommand.Create of(Long userId, List<OrderCommand.CartItem> items, Long couponId) {
            return new OrderCommand.Create(userId, items, couponId);
        }
    }

    public record CartItem(Long productId, Long quantity) {

    }
}
