package com.loopers.domain.common.event;

public record OutBoundEvent(
    String topic,
    String key,
    String eventType,
    Object payload
) {

}
