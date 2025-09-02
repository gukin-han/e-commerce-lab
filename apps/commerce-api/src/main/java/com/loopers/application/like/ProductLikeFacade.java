package com.loopers.application.like;

import com.loopers.domain.like.ProductLikeEvent;
import com.loopers.domain.like.ProductLikeRepository;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserService;
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
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public ProductLikeResult.Like like(ProductLikeCommand.Like command) {
        User user = userService.getByLoginId(command.loginId());

        // 1. 상품 좋아요 등록
        boolean isInserted = productLikeRepository.insertIgnoreDuplicateKey(user.getId(), command.productId());

        // 2. 상품 좋아요 수 증가
        if (isInserted) {
            eventPublisher.publishEvent(new ProductLikeEvent.Added(command.productId(), UUID.randomUUID(), Instant.now()));
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
            eventPublisher.publishEvent(new ProductLikeEvent.Deleted(command.productId(), UUID.randomUUID(), Instant.now()));
            return ProductLikeResult.Unlike.success();
        }

        return ProductLikeResult.Unlike.alreadyUnliked();
    }
}
