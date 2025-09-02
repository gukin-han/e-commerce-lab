package com.loopers.infrastructure.data.product;

import com.loopers.domain.product.Product;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ProductJpaRepository extends JpaRepository<Product, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
           select p
             from Product p
            where p.id in :productIds
           """)
    List<Product> findAllForUpdate(List<Long> productIds);


    @Modifying
    @Query(value = "UPDATE products SET like_count = like_count + 1 WHERE id = :productId", nativeQuery = true)
    int incrementLikeCount(Long productId);

    @Modifying
    @Query(value = "UPDATE products SET like_count = GREATEST(like_count - 1, 0) WHERE id = :productId", nativeQuery = true)
    int decrementLikeCount(Long productId);

    long countByBrandId(Long brandId);
}
