package com.loopers.interfaces.listener.rank;

public final class OrderEvent {
    private OrderEvent() {}

    /**
     * 주문 발생 이벤트
     * @param productId 상품 ID
     */
    public record Ordered(
        Long productId
    ) {}
}
