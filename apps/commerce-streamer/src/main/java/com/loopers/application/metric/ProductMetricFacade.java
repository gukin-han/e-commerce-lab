package com.loopers.application.metric;

import com.loopers.domain.event.EventHandledRepository;
import com.loopers.domain.metric.ProductMetricRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductMetricFacade {

  private final ProductMetricRepository productMetricRepository;
  private final EventHandledRepository eventHandledRepository;

  @Transactional
  public void increaseLikeCount(ProductMetricCommand.Change command) {
    // 1) 멱등키 삽입 시도: 실패(중복)이면 바로 return
    if (!eventHandledRepository.insertIgnoreInto(command.eventId())) {
      log.info("[increaseLIkeCount] Product metric event {} already exists", command.eventId());
      return;
    }
    // 2) 최초 처리만 메트릭 반영
    productMetricRepository.increaseLikeCountByProductId(command.productId(), command.metricDate());
  }

  @Transactional
  public void decreaseLikeCount(ProductMetricCommand.Change command) {
    if (!eventHandledRepository.insertIgnoreInto(command.eventId())) {
      log.info("[decreaseLIkeCount] Product metric event {} already exists", command.eventId());
      return;
    }
    productMetricRepository.decreaseLikeCountByProductId(command.productId(), command.metricDate());
  }
}
