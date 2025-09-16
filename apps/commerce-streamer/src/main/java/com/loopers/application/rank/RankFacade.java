package com.loopers.application.rank;

import com.loopers.domain.rank.Rank;
import com.loopers.domain.rank.RankInMemoryRepository;
import com.loopers.domain.rank.Score;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RankFacade {

  private final RankInMemoryRepository rankInMemoryRepository;

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
    rankInMemoryRepository.incrementScore(dailyKey, member, score.getScore(), Duration.ofDays(2));
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
    Set<Rank> topRanks = rankInMemoryRepository.getTopRanks(todayKey, topK);
    if (topRanks.isEmpty()) {
      return;
    }

    // 내일 키 절대 만료 (내일 00:00 + 2일)
    Instant expireAt = tomorrow.plusDays(2)
        .atStartOfDay(ZONE_KST)
        .toInstant();

    Map<String, Double> memberScores = topRanks.stream()
        .collect(Collectors.toMap(
            Rank::getMember,
            rank -> rank.getScore() * c
        ));

    if (memberScores.isEmpty()) {
        return;
    }

    rankInMemoryRepository.incrementScores(tomorrowKey, memberScores, expireAt);
  }
}
