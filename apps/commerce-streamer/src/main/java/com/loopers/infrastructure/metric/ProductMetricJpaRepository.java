package com.loopers.infrastructure.metric;

import com.loopers.domain.metric.ProductMetric;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductMetricJpaRepository extends JpaRepository<ProductMetric, Long> {

  @Modifying
  @Query(value = """
      INSERT INTO product_metrics (
          product_id, metric_date, like_count,
          view_count, order_count, payment_count, purchase_count,
          created_at, updated_at
      ) VALUES (:productId, :metricDate, 1, 0, 0, 0, 0, NOW(), NOW())
      ON DUPLICATE KEY UPDATE
          like_count = like_count + 1,
          updated_at = NOW()
      """, nativeQuery = true)
  void upsertIncreaseLikeCount(
      @Param("productId") Long productId,
      @Param("metricDate") LocalDate metricDate
  );

  @Modifying
  @Query(value = """
      INSERT INTO product_metrics (
          product_id, metric_date, like_count,
          view_count, order_count, payment_count, purchase_count,
          created_at, updated_at
      ) VALUES (:productId, :metricDate, 0, 0, 0, 0, 0, NOW(), NOW())
      ON DUPLICATE KEY UPDATE
          like_count = IF(like_count > 0, like_count - 1, 0),
          updated_at = NOW()
      """, nativeQuery = true)
  void upsertDecreaseLikeCount(
      @Param("productId") Long productId,
      @Param("metricDate") LocalDate metricDate
  );

  Optional<ProductMetric> findByProductIdAndMetricDate(Long productId, LocalDate metricDate);
}
