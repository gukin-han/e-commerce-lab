package com.loopers.application.metric;

import java.time.LocalDate;

public class ProductMetricCommand {

  public record Change(Long productId, String eventId, LocalDate metricDate) {


  }

}
