package com.loopers.domain.like;

import com.loopers.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "product_likes", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "product_id"}))
public class ProductLike extends BaseEntity {

    private Long userId;
    private Long productId;

    @Builder
    private ProductLike(Long userId, Long productId) {
        this.userId = userId;
        this.productId = productId;
    }

    public static ProductLike create(Long userId, Long productId) {
        return ProductLike.builder()
                .userId(userId)
                .productId(productId)
                .build();
    }

    public ProductLikeId getProductLikeId() {
        return getId() == null ? null : ProductLikeId.of(getId());
    }
}
