package com.loopers.interfaces.api.like;

import com.loopers.application.like.ProductLikeResult;

public class ProductLikeV1Response {

    public record Like() {
        public static Like fromResult(ProductLikeResult.Like result) {
            return null;
        }
    }

    public record Unlike() {
        public static Unlike fromResult(ProductLikeResult.Unlike result) {
            return null;
        }
    }
}
