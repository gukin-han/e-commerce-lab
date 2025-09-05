package com.loopers.interfaces.listener.stock;

public class StockEvent {

  public record changed(Long productId, Long stock, String status) {


  }

}
