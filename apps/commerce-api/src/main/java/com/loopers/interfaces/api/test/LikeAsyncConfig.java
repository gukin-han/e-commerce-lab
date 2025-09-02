package com.loopers.interfaces.api.test;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class LikeAsyncConfig {

    @Bean(name = "likeEventExecutor")
    public Executor likeEventExecutor() {
        ThreadPoolTaskExecutor ex = new ThreadPoolTaskExecutor();
        ex.setCorePoolSize(8);       // 서버 코어/부하에 맞게 조정
        ex.setMaxPoolSize(32);
        ex.setQueueCapacity(20000);  // 큐 길게: 큐잉 → TTV에 반영
        ex.setThreadNamePrefix("like-ev-");
        ex.initialize();
        return ex;
    }
}
