package com.loopers.domain.product;

import com.loopers.application.product.dto.ProductSortType;

import java.util.List;
import java.util.Optional;

public interface ProductRepository {
    Optional<Product> findById(Long productId);

    List<ProductDetail> findPagedProductDetails(Long brandId, int page, int size, ProductSortType productSortType);

    Product save(Product product);

    long getTotalCountByBrandId(Long brandId);

    List<Product> saveAll(List<Product> products);

    List<Product> findAllByIdsWithPessimisticLock(List<Long> productIds);

    boolean incrementLikeCount(Long productId);

    boolean decrementLikeCount(Long productId);

    List<Product> findAllById(List<Long> productIds);

    void updateLikeCount(Long productId, long count);

    Long getLikeCount(Long productId);
}
