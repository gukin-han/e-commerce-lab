package com.loopers.interfaces.scheduler.rank;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WeeklyRankScheduler {

  private final JobLauncher jobLauncher;
  private final Job weeklyRankJob;

  @Scheduled(cron = "0 5 6 * * *", zone = "Asia/Seoul") // 매주 월요일 06:05 KST에 실행
  public void runWeekly() {

  }

}
