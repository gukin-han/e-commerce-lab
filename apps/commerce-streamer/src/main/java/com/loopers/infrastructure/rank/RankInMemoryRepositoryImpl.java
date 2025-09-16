package com.loopers.infrastructure.rank;

import com.loopers.domain.rank.Rank;
import com.loopers.domain.rank.RankInMemoryRepository;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class RankInMemoryRepositoryImpl implements RankInMemoryRepository {

    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public void incrementScore(String key, String member, double score, Duration ttl) {
        redisTemplate.opsForZSet().incrementScore(key, member, score);
        redisTemplate.expire(key, ttl);
    }

    @Override
    public Set<Rank> getTopRanks(String key, int topK) {
        Set<TypedTuple<String>> tuples = redisTemplate.opsForZSet().reverseRangeWithScores(key, 0, topK - 1);
        if (tuples == null) {
            return Set.of();
        }
        return tuples.stream()
            .filter(t -> t.getValue() != null && t.getScore() != null)
            .map(t -> new Rank(t.getValue(), t.getScore()))
            .collect(Collectors.toSet());
    }

    @Override
    public void incrementScores(String key, Map<String, Double> memberScores, Instant expireAt) {
        ZSetOperations<String, String> zset = redisTemplate.opsForZSet();
        memberScores.forEach((member, score) -> {
            if (score != null && score != 0.0 && !Double.isNaN(score) && !Double.isInfinite(score)) {
                zset.incrementScore(key, member, score);
            }
        });
        redisTemplate.expireAt(key, expireAt);
    }
}
