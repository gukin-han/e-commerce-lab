package com.loopers.domain.rank;

import org.springframework.data.redis.core.ZSetOperations.TypedTuple;

import java.util.Set;

public interface RankInMemoryRepository {
    Set<TypedTuple<String>> findTopRankings(String key, long start, long end);

    Long findRankByProductId(String key, Long productId);
}
