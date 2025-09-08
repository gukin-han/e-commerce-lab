package com.loopers.domain.like;

import java.time.Instant;
import java.util.UUID;

public class ProductLikeEvent {
    public record Added(
            Long productId
    ) {}

    public record Deleted(
            Long productId
    ) {}
}
