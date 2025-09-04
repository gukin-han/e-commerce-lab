package com.loopers.application.like;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import com.loopers.common.constant.Gender;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserRepository;
import com.loopers.domain.user.UserService;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.stream.StreamSupport;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.testcontainers.shaded.org.awaitility.Awaitility;

@SpringBootTest
class ProductLikeFacadeTest {

  @Autowired
  UserRepository userRepository;

  @Autowired
  ProductLikeFacade sut;

  @Autowired
  KafkaProperties kafkaProperties;

  private Consumer<String, Object> consumer;

  @BeforeEach
  void setUpConsumer() {
    HashMap<String, Object> props = new HashMap<>(kafkaProperties.buildConsumerProperties());
    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
    props.put(ConsumerConfig.GROUP_ID_CONFIG, "test-group");
    props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
    consumer = new DefaultKafkaConsumerFactory<String, Object>(props).createConsumer();
    consumer.subscribe(List.of("like-events"));
  }

  @DisplayName("상품 좋아요")
  @Nested
  class Like {

    @DisplayName("커밋 이후에 좋아요 이벤트를 발행한다")
    @Test
    void publishLikeEvent_afterCommit() {
      // given
      ProductLikeCommand.Like command = ProductLikeCommand.Like.builder()
          .productId(1L)
          .loginId("gukin")
          .build();

      userRepository.save(User.create("gukin", "test@gmail.com", "2025-01-01", Gender.MALE));

      // when
      sut.like(command);

      // then: AFTER_COMMIT 비동기 → 토픽에서 실제 수신될 때까지 대기
      Awaitility.await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> {
        var records = consumer.poll(Duration.ofMillis(250));
        assertThat(records.count()).isGreaterThan(0);
        System.out.println(records.toString());

        boolean found = StreamSupport.stream(records.spliterator(), false)
            .anyMatch(rec ->
                String.valueOf(1L).equals(rec.key()) &&
                    rec.value() instanceof String v &&
                    v.contains("\"eventType\":\"like.added.v1\"")
            );
        assertThat(found).isTrue();
      });

    }

  }



}