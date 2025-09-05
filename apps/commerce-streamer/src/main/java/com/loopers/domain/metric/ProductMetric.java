package com.loopers.domain.metric;

import com.loopers.domain.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
    name = "product_metrics",
    uniqueConstraints = @UniqueConstraint(columnNames = {"productId", "metricDate"})
)
@Entity
public class ProductMetric extends BaseEntity {

  private Long productId;

  private LocalDate metricDate;

  private long likeCount;

  private long viewCount;

  private long orderCount;

  private long paymentCount;

  private long purchaseCount;
}
