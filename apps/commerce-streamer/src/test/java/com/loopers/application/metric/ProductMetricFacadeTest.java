package com.loopers.application.metric;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import com.loopers.application.metric.ProductMetricCommand.Change;
import com.loopers.domain.metric.ProductMetric;
import com.loopers.domain.metric.ProductMetricRepository;
import com.loopers.utils.DatabaseCleanUp;
import java.time.LocalDate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ProductMetricFacadeTest {

  @Autowired
  ProductMetricFacade sut;

  @Autowired
  ProductMetricRepository productMetricRepository;

  @Autowired
  DatabaseCleanUp databaseCleanUp;

  @AfterEach
  void tearDown() {
    databaseCleanUp.truncateAllTables();
  }

  @DisplayName("좋아요 수 증가 이벤트 커멘드 입력시")
  @Nested
  class IncreaseLikeCount {

    @DisplayName("중복없이 증가한다")
    @Test
    void increaseWithoutDuplicate() {
      // given
      LocalDate currentDate = LocalDate.now();
      Change command = new Change(1L, "eventId", currentDate);

      // when
      sut.increaseLikeCount(command);
      sut.increaseLikeCount(command);

      // then
      ProductMetric metric = productMetricRepository.findByProductIdAndMetricDate(
          command.productId(),
          command.metricDate()
      ).get();

      Assertions.assertAll(
          () -> assertThat(metric.getMetricDate()).isEqualTo(command.metricDate()),
          () -> assertThat(metric.getLikeCount()).isEqualTo(1L)
      );

    }

  }

}