package com.loopers.application.like;

import lombok.Builder;

public class ProductLikeCommand {

    @Builder
    public record Like(String loginId, Long productId) {
        public static ProductLikeCommand.Like of(String loginId, Long productId) {
            return ProductLikeCommand.Like.builder()
                    .loginId(loginId)
                    .productId(productId)
                    .build();
        }
    }

    @Builder
    public record Unlike(String loginId, Long productId) {
        public static ProductLikeCommand.Unlike of(String loginId, Long productId) {
            return ProductLikeCommand.Unlike.builder()
                    .loginId(loginId)
                    .productId(productId)
                    .build();
        }
    }
}
