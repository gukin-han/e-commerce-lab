package com.loopers.domain.common.event;

public interface EventPublisher {
    void publish(String topic, String key, Object payload);
}
