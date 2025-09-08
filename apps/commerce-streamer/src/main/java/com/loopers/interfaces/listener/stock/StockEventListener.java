package com.loopers.interfaces.listener.stock;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopers.Envelope;
import com.loopers.application.stock.StockFacade;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class StockEventListener {

  private final ObjectMapper objectMapper;
  private final StockFacade stockFacade;

  @KafkaListener(
      topics = "stock-events"
  )
  public void handleStockChanged(ConsumerRecord<String, String> record, Acknowledgment ack) {
    try {
      // 1. Envelope
      Envelope<StockEvent.changed> envelope = objectMapper.readValue(record.value(), new TypeReference<>() {});

      // 2. payload를 구체 타입으로 역직렬화
      String eventType = envelope.eventType();
      StockEvent.changed  command = envelope.payload();
      if ("stock.decreased.v1".equals(eventType)) {
        stockFacade.handleStockChange(command);
      }
      ack.acknowledge();

    } catch (Exception e) {
      log.error("Failed to parse event payload: {}", record.value(), e);
    }
  }
}
