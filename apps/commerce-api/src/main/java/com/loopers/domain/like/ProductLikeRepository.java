package com.loopers.domain.like;

import java.util.Optional;

public interface ProductLikeRepository {
    ProductLike save(ProductLike productLike);
    Optional<ProductLike> findByUserIdAndProductId(Long userId, Long productId);

    boolean insertIgnoreDuplicateKey(Long userId, Long productId);

    boolean deleteByProductIdAndUserId(Long userId, Long productId);
}
