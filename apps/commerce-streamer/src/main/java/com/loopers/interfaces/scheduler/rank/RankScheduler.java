package com.loopers.interfaces.scheduler.rank;

import com.loopers.application.rank.RankFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RankScheduler {

  public static final int TOP_K = 1000;
  private final RankFacade rankFacade;

  // 매일 23:50 KST에 캐리오버 시도
  @Scheduled(cron = "0 50 23 * * *", zone = "Asia/Seoul")
  public void carryOverDailyWarmup() {
    // c=0.10, K=1000 (값은 yml/외부화 필요 - 지금은 하드코딩)
    rankFacade.carryOverDaily(0.10, TOP_K);
  }

}
