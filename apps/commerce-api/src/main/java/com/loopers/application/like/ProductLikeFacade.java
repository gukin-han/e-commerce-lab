package com.loopers.application.like;

import com.loopers.domain.common.event.EventPublisher;
import com.loopers.domain.common.event.OutBoundEvent;
import com.loopers.domain.like.ProductLikeEvent;
import com.loopers.domain.like.ProductLikeRepository;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserService;
import com.loopers.infrastructure.event.AfterCommitEventRelay;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ProductLikeFacade {

    private final UserService userService;
    private final ProductLikeRepository productLikeRepository;
    private final ProductRepository productRepository;
    private final AfterCommitEventRelay eventRelay;

    @Transactional
    public ProductLikeResult.Like like(ProductLikeCommand.Like command) {
        User user = userService.getByLoginId(command.loginId());

        // 1. 상품 좋아요 등록
        boolean isInserted = productLikeRepository.insertIgnoreDuplicateKey(user.getId(), command.productId());

        // 2. 상품 좋아요 수 증가
        if (isInserted) {
            productRepository.incrementLikeCount(command.productId());

            // 스프링 이벤트 발행(AFTER_COMMIT) -> 카프카 이벤트 발행
            eventRelay.on(new OutBoundEvent(
                "like-events",
                command.productId().toString(),
                "like.added.v1",
                new ProductLikeEvent.Added(command.productId())
                )
            );

            return ProductLikeResult.Like.success();
        }

        return ProductLikeResult.Like.alreadyLiked();
    }

    @Transactional
    public ProductLikeResult.Unlike unlike(ProductLikeCommand.Unlike command) {
        User user = userService.getByLoginId(command.loginId());

        // 1. 상품 좋아요 삭제
        boolean isDeleted = productLikeRepository.deleteByProductIdAndUserId(user.getId(), command.productId());
        if (isDeleted) {
            // 2. 상품 좋아요 수 감소
            productRepository.decrementLikeCount(command.productId());

            // 스프링 이벤트 발행(AFTER_COMMIT) -> 카프카 이벤트 발행
            eventRelay.on(new OutBoundEvent(
                "like-events", command.productId().toString(),
                "like.deleted.v1",
                new ProductLikeEvent.Deleted(command.productId())
                )
            );

            return ProductLikeResult.Unlike.success();
        }

        return ProductLikeResult.Unlike.alreadyUnliked();
    }
}
