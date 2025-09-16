package com.loopers.application.rank;

import java.time.Instant;

public class RankCommand {

  public record Update(
      Long productId,
      Instant occurredAt,           // ← 반드시 포함: 윈도우 키 계산용
      int viewDelta,                // 조회 증분 (보통 0 또는 +1)
      int likeDelta,                // 좋아요 증분 (+1 / -1)
      int orderCountDelta,          // 주문 건수 증분 (보통 0 또는 +1)
      String scope                  // 선택: 카테고리/채널 등 가중치 스코프(없으면 null)
  ) {

    public static final String ALL = "all";

    public static Update create(Long productId, Instant occurredAt, int viewDelta, int likeDelta, int orderCountDelta, String scope) {
      if (productId == null) {
        throw new IllegalArgumentException("productId cannot be null");
      }

      if (occurredAt == null) {
        throw new IllegalArgumentException("occurredAt cannot be null");
      }

      if (scope.isBlank()) {
        scope = ALL;
      }

      return new Update(productId, occurredAt, viewDelta, likeDelta, orderCountDelta, scope);
    }

  }

}
