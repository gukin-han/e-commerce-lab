package com.loopers.interfaces.event;

import com.loopers.application.product.ProductFacade;
import com.loopers.domain.like.ProductLikeEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;


@Slf4j
@Component
@RequiredArgsConstructor
public class ProductLikeEventListener {

    private final ProductFacade productFacade;

    @Async("likeEventExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleAddedEvent(ProductLikeEvent.Added message) {
        try {
            productFacade.increaseLikeCount(message.productId());
        } catch (Exception e) {
            log.error("Failed to process ProductLikeAdded: productId={}, eventId={}", message.productId(), message.eventId(), e);
            throw e;
        }
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleDeletedEvent(ProductLikeEvent.Deleted message) {
        try {
            productFacade.decreaseLikeCount(message.productId());
        } catch (Exception e) {
            log.error("Failed to process ProductLikeDeleted: productId={}, eventId={}", message.productId(), message.eventId(), e);
            throw e;
        }
    }
}
