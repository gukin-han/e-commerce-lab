package com.loopers.domain.like;

import java.time.Instant;
import java.util.UUID;

public class ProductLikeEvent {
    public record Added(
            Long productId,
            UUID eventId,
            Instant occurredAt
    ) {}

    public record Deleted(
            Long productId,
            UUID eventId,
            Instant occurredAt
    ) {}
}
