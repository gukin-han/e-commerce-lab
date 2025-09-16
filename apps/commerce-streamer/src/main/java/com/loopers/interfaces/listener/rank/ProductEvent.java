package com.loopers.interfaces.listener.rank;

public final class ProductEvent {
    private ProductEvent() {}

    /**
     * 상품 조회 이벤트
     * @param productId 상품 ID
     */
    public record Viewed(
        Long productId
    ) {}
}
