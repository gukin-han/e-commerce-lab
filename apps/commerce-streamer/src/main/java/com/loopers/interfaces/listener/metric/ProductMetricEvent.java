package com.loopers.interfaces.listener.metric;

public class ProductMetricEvent {

  public record Like(
      Long productId
  ) {

  }

}
