package com.loopers.infrastructure.metric;

import com.loopers.domain.metric.ProductMetric;
import com.loopers.domain.metric.ProductMetricRepository;
import java.time.LocalDate;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductMetricRepositoryImpl implements ProductMetricRepository {

  private final ProductMetricJpaRepository productMetricJpaRepository;

  @Override
  public void increaseLikeCountByProductId(Long productId, LocalDate metricDate) {
    productMetricJpaRepository.upsertIncreaseLikeCount(productId, metricDate);
  }

  @Override
  public void decreaseLikeCountByProductId(Long productId, LocalDate metricDate) {
    productMetricJpaRepository.upsertDecreaseLikeCount(productId, metricDate);
  }

  @Override
  public Optional<ProductMetric> findByProductIdAndMetricDate(Long productId, LocalDate metricDate) {
    return productMetricJpaRepository.findByProductIdAndMetricDate(productId, metricDate);
  }
}
