package com.loopers.infrastructure.data.like;

import com.loopers.domain.like.ProductLike;
import com.loopers.domain.like.ProductLikeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@RequiredArgsConstructor
@Component
public class ProductLikeRepositoryImpl implements ProductLikeRepository {

    private final ProductLikeJpaRepository productLikeJpaRepository;

    @Override
    public ProductLike save(ProductLike productLike) {
        return productLikeJpaRepository.save(productLike);
    }

    @Override
    public Optional<ProductLike> findByUserIdAndProductId(Long userId, Long productId) {
        return productLikeJpaRepository.findByUserIdAndProductId(userId, productId);
    }

    @Override
    public boolean insertIgnoreDuplicateKey(Long userId, Long productId) {
        return productLikeJpaRepository.insertIgnoreDuplicateKey(userId, productId) > 0;
    }

    @Override
    public boolean deleteByProductIdAndUserId(Long userId, Long productId) {
        return productLikeJpaRepository.deleteByProductIdAndUserId(productId, userId) > 0;
    }

}
