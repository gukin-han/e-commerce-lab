package com.loopers.domain.rank;

import com.loopers.domain.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Index;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Column;
import java.time.LocalDate;
import java.math.BigDecimal;
import lombok.Getter;

@Getter
@Entity
@Table(
    name = "product_rank_weekly",
    indexes = {
        @Index(name = "idx_mv_weekly_base_rank", columnList = "base_date, rank_no"),
        @Index(name = "idx_mv_weekly_product_date", columnList = "product_id, base_date")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uq_mv_weekly_product_date", columnNames = {"product_id", "base_date"})
    }
)
public class ProductRankWeekly extends BaseEntity {
  @Column(name = "product_id", nullable = false)
  private Long productId;

  @Column(name = "base_date", nullable = false)
  private LocalDate baseDate;

  @Column(name = "score", nullable = false, precision = 18, scale = 6)
  private BigDecimal score;

  @Column(name = "rank_no", nullable = false)
  private Integer rank;
}
