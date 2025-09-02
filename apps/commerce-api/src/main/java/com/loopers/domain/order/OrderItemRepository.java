package com.loopers.domain.order;

import java.util.List;

public interface OrderItemRepository {
    void saveAll(List<OrderItem> orderItems);

    List<OrderItem> findOrderItemsByOrderId(Long orderId);
}
