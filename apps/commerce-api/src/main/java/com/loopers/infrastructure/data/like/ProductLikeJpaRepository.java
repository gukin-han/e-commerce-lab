package com.loopers.infrastructure.data.like;

import com.loopers.domain.like.ProductLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ProductLikeJpaRepository extends JpaRepository<ProductLike, Long> {
    Optional<ProductLike> findByUserIdAndProductId(Long userId, Long productId);

    @Modifying
    @Query(value = """
        INSERT IGNORE INTO product_likes (user_id, product_id, created_at, updated_at)
        VALUES (:userId, :productId, NOW(), NOW())
        """, nativeQuery = true)
    int insertIgnoreDuplicateKey(Long userId, Long productId);

    int deleteByProductIdAndUserId(Long productId, Long userId);

}
