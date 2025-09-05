package com.loopers.domain.product;

public class ProductEvent {

  public record StockChanged(Long productId, Long stock, String status) {
  }

}
