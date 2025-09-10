package com.loopers.application.rank;

import com.loopers.domain.rank.Score;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RankFacade {

  private final RedisTemplate<String, String> redisTemplate;

  private static final ZoneId ZONE_KST = ZoneId.of("Asia/Seoul");
  private static final DateTimeFormatter DAILY_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd").withZone(ZONE_KST);
  private static final String PREFIX = "ranking";

  public void updateRank(
      RankCommand.Update command
  ) {

    // 1. 스코어 계산
    Score score = Score.create(
        command.viewDelta(),
        command.likeDelta(),
        command.orderCountDelta()
    );

    // 2. 윈도우 키 계산(일간 기준)
    String dailyKey = this.dailyKey(PREFIX, command.scope(), DAILY_FORMATTER.format(command.occurredAt()));
    String member = String.valueOf(command.productId());

    // 3. ZSET 증분 + TTL 보장 (단건)
    redisTemplate.opsForZSet().incrementScore(dailyKey, member, score.getScore());
    // 키 생성/업데이트 시마다 TTL 갱신해도 부담 적음
    redisTemplate.expire(dailyKey, Duration.ofDays(2));
  }

  public String dailyKey(String prefix, String scope, String window) {
    return String.format("%s:%s:%s",
        prefix,
        scope,
        window
    );
  }

  public void carryOverDaily(double c, int topK) {
    // KST 기준 날짜
    LocalDate today = LocalDate.now(ZONE_KST);
    LocalDate tomorrow = today.plusDays(1);

    final String scope = "all";

    String todayKey = dailyKey(PREFIX, scope, DAILY_FORMATTER.format(today));
    String tomorrowKey = dailyKey(PREFIX, scope, DAILY_FORMATTER.format(tomorrow));

    // 오늘 Top-K 읽기
    Set<TypedTuple<String>> tuples =
        redisTemplate.opsForZSet().reverseRangeWithScores(todayKey, 0, topK - 1);
    if (tuples == null || tuples.isEmpty()) {
      return;
    }

    // 내일 키 절대 만료 (내일 00:00 + 2일)
    Instant expireAt = tomorrow.plusDays(2)
        .atStartOfDay(ZONE_KST)
        .toInstant();

    // 파이프라인을 사용하지 않는 간단한 루프 방식으로 변경
    // 이 방식은 각 항목에 대해 개별 Redis 호출이 발생
    ZSetOperations<String, String> zset = redisTemplate.opsForZSet();
    for (TypedTuple<String> t : tuples) {
      String member = t.getValue();
      Double score = t.getScore();
      if (member == null || score == null) {
        continue;
      }

      // 내일 랭킹으로 이전할 스코어(c는 감쇠 계수, 예: 0.1)
      double delta = score * c;
      if (delta == 0.0 || Double.isNaN(delta) || Double.isInfinite(delta)) {
        continue;
      }

      zset.incrementScore(tomorrowKey, member, delta);
    }

    // 내일 랭킹 데이터의 만료 시간을 설정
    redisTemplate.expireAt(tomorrowKey, expireAt);
  }
}
