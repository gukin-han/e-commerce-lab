package com.loopers.domain.metric;

import java.time.LocalDate;
import java.util.Optional;

public interface ProductMetricRepository {

  void increaseLikeCountByProductId(Long productId, LocalDate metricDate);
  void decreaseLikeCountByProductId(Long productId, LocalDate metricDate);

  Optional<ProductMetric> findByProductIdAndMetricDate(Long productId, LocalDate localDate);
}
