package com.loopers.infrastructure.rank;

import com.loopers.domain.rank.RankInMemoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
@RequiredArgsConstructor
public class RankInMemoryRepositoryImpl implements RankInMemoryRepository {

    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public Set<ZSetOperations.TypedTuple<String>> findTopRankings(String key, long start, long end) {
        return redisTemplate.opsForZSet().reverseRangeWithScores(key, start, end);
    }

    @Override
    public Long findRankByProductId(String key, Long productId) {
        Long rank = redisTemplate.opsForZSet().reverseRank(key, productId.toString());
        return rank != null ? rank + 1 : null;
    }
}
