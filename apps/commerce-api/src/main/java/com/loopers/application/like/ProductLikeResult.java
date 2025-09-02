package com.loopers.application.like;

public class ProductLikeResult {

    public record Like() {
        public static ProductLikeResult.Like alreadyLiked() {
            return null;
        }

        public static ProductLikeResult.Like success() {
            return null;
        }
    }

    public record Unlike() {
        public static ProductLikeResult.Unlike alreadyUnliked() {
            return null;
        }

        public static ProductLikeResult.Unlike success() {
            return null;
        }
    }
}
