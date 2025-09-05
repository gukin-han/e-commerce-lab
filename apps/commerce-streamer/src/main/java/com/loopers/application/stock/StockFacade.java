package com.loopers.application.stock;

import com.loopers.interfaces.listener.stock.StockEvent.changed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class StockFacade {

  public static final String SOLD_OUT = "SOLD_OUT";
  private final RedisTemplate<String, Object> redisTemplate;

  public void handleStockChange(changed command) {
    String cacheKey = buildCacheKey(command);

    // TODO: 캐시 evict 처리 패키징 위치에 대한 고민
    if (SOLD_OUT.equals(command.status())) {
      redisTemplate.delete(cacheKey);
    }

    log.info("Evicted Redis cache key={}", cacheKey);
  }


  private String buildCacheKey(changed command) {
    // 필요에 따라 키 생성 로직 추가
    return "product:" + command.productId().toString();
  }
}
