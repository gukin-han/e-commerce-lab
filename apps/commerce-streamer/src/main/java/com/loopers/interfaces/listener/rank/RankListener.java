package com.loopers.interfaces.listener.rank;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.loopers.Envelope;
import com.loopers.application.rank.RankCommand;
import com.loopers.application.rank.RankFacade;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RankListener {

  private final RankFacade rankFacade;

  private static final ObjectMapper objectMapper = new ObjectMapper()
      .registerModule(new JavaTimeModule())
      .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

  // 윈도우 키 생성을 위해 Facade와 동일한 포맷터를 사용합니다.
  private static final ZoneId ZONE_KST = ZoneId.of("Asia/Seoul");
  private static final DateTimeFormatter DAILY_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd").withZone(ZONE_KST);

  /**
   * 집계 키 (상품 ID + 윈도우)
   */
  private record AggregationKey(Long productId, String window) {}

  @KafkaListener(
      topics = {"product.event.v1", "like.event.v1", "order.event.v1"},
      containerFactory = "kafkaBatchFactory"
  )
  public void consume(List<ConsumerRecord<String, String>> records) {
    // 1. (productId, window) 별로 집계하기 위한 Map
    Map<AggregationKey, RankUpdateAggregator> aggregators = new HashMap<>();

    // 2. 메시지를 하나씩 역직렬화하고 집계
    for (ConsumerRecord<String, String> record : records) {
      String message = record.value();
      try {
        Envelope<JsonNode> envelope = objectMapper.readValue(message, new TypeReference<>() {});
        aggregateEvents(envelope, aggregators);
      } catch (JsonProcessingException e) {
        log.warn("Failed to deserialize message: {}. Record: {}", message, record, e);
      } catch (Exception e) {
        log.error("Error processing message: {}. Record: {}", message, record, e);
      }
    }

    // 3. 집계된 결과를 바탕으로 랭킹 업데이트
    aggregators.forEach((key, aggregator) -> {
      RankCommand.Update command = aggregator.toCommand(key);
      rankFacade.updateRank(command);
    });
  }

  /**
   * Kafka 메시지에 포함된 단일 이벤트를 처리하여 집계 맵에 반영합니다.
   * <p>
   * Envelope에서 이벤트 타입과 페이로드를 추출하고, 이벤트 종류에 따라 적절한 델타 값을 계산합니다.
   * 이벤트 발생 시각을 기준으로 일간 윈도우 키를 생성하고, (productId, window)를 복합 키로 사용하여
   * 해당 윈도우의 집계기에 델타 값을 더합니다.
   *
   * @param envelope    역직렬화된 Envelope 객체 (페이로드는 JsonNode 형태)
   * @param aggregators 집계 데이터를 누적할 맵
   * @throws JsonProcessingException 페이로드(JsonNode)를 특정 이벤트 DTO로 변환하는 데 실패할 경우 발생
   */
  private void aggregateEvents(Envelope<JsonNode> envelope, Map<AggregationKey, RankUpdateAggregator> aggregators) throws JsonProcessingException {
    String eventType = envelope.eventType();
    JsonNode payloadNode = envelope.payload();
    Instant occurredAt = envelope.occurredAt();

    Long productId = null;
    int viewDelta = 0;
    int likeDelta = 0;
    int orderDelta = 0;

    switch (eventType) {
      case "viewed" -> {
        ProductEvent.Viewed payload = objectMapper.treeToValue(payloadNode, ProductEvent.Viewed.class);
        productId = payload.productId();
        viewDelta = 1;
      }
      case "likeAdded" -> {
        LikeEvent.LikeAdded payload = objectMapper.treeToValue(payloadNode, LikeEvent.LikeAdded.class);
        productId = payload.productId();
        likeDelta = 1;
      }
      case "likeDeleted" -> {
        LikeEvent.LikeDeleted payload = objectMapper.treeToValue(payloadNode, LikeEvent.LikeDeleted.class);
        productId = payload.productId();
        likeDelta = -1;
      }
      case "ordered" -> {
        OrderEvent.Ordered payload = objectMapper.treeToValue(payloadNode, OrderEvent.Ordered.class);
        productId = payload.productId();
        orderDelta = 1;
      }
      default -> log.warn("Unknown eventType: {}", eventType);
    }

    if (productId != null) {
      String window = DAILY_FORMATTER.format(occurredAt);
      AggregationKey key = new AggregationKey(productId, window);
      aggregators.computeIfAbsent(key, k -> new RankUpdateAggregator())
          .add(viewDelta, likeDelta, orderDelta);
    }
  }

  /**
   * 배치 내에서 (productId, window)별 랭킹 업데이트 데이터를 집계하는 private 헬퍼 클래스
   */
  private static class RankUpdateAggregator {
    int viewDelta = 0;
    int likeDelta = 0;
    int orderCountDelta = 0;

    void add(int view, int like, int order) {
      this.viewDelta += view;
      this.likeDelta += like;
      this.orderCountDelta += order;
    }

    RankCommand.Update toCommand(AggregationKey key) {
      // 커맨드를 생성할 때는 윈도우의 시작 시각을 사용합니다.
      Instant windowInstant = LocalDate.parse(key.window(), DAILY_FORMATTER)
          .atStartOfDay(ZONE_KST).toInstant();

      return RankCommand.Update.create(
          key.productId(),
          windowInstant,
          viewDelta,
          likeDelta,
          orderCountDelta,
          RankCommand.Update.ALL
      );
    }
  }
}
