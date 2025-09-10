package com.loopers.interfaces.listener.rank;

public final class LikeEvent {
    private LikeEvent() {}

    /**
     * 좋아요 추가 이벤트
     * @param productId 상품 ID
     */
    public record LikeAdded(
        Long productId
    ) {}

    /**
     * 좋아요 삭제 이벤트
     * @param productId 상품 ID
     */
    public record LikeDeleted(
        Long productId
    ) {}
}
