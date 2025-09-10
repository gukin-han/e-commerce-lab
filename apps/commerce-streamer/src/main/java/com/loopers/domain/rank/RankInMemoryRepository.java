package com.loopers.domain.rank;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Set;

public interface RankInMemoryRepository {

    void incrementScore(String key, String member, double score, Duration ttl);

    Set<Rank> getTopRanks(String key, int topK);

    void incrementScores(String key, Map<String, Double> memberScores, Instant expireAt);

}
