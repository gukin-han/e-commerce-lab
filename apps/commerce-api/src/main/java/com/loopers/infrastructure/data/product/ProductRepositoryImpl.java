package com.loopers.infrastructure.data.product;

import com.loopers.application.product.dto.ProductSortType;
import com.loopers.domain.product.ProductDetail;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductRepository;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

import static com.loopers.domain.brand.QBrand.brand;
import static com.loopers.domain.product.QProduct.product;


@Component
@RequiredArgsConstructor
public class ProductRepositoryImpl implements ProductRepository {

    private final ProductJpaRepository productJpaRepository;
    private final JPAQueryFactory jpaQueryFactory;

    // SELECT
    @Override
    public Optional<Product> findById(Long productId) {
        return productJpaRepository.findById(productId);
    }

    @Override
    public List<ProductDetail> findPagedProductDetails(Long brandId, int page, int size, ProductSortType productSortType) {
        return jpaQueryFactory
                .select(Projections.constructor(ProductDetail.class,
                        product.id,
                        product.name,
                        product.price,
                        product.stock,
                        product.likeCount,
                        brand.id,
                        brand.name
                ))
                .from(product)
                .innerJoin(brand).on(product.brandId.eq(brand.id))
                .where(brandEq(brandId))
                .orderBy(productSort(productSortType))
                .offset((long) page * size)
                .limit(size)
                .fetch();
    }

    private BooleanExpression brandEq(Long brandId) {
        return brandId == null ? null : brand.id.eq(brandId);
    }

    @Override
    public long getTotalCountByBrandId(Long brandId) {
        if (brandId == null) {
            return productJpaRepository.count();
        } else {
            return productJpaRepository.countByBrandId(brandId);
        }
    }

    @Override
    public List<Product> findAllByIdsWithPessimisticLock(List<Long> productIds) {
        return productJpaRepository.findAllForUpdate(productIds);
    }

    // INSERT
    @Override
    public Product save(Product product) {
        return productJpaRepository.save(product);
    }

    @Override
    public List<Product> saveAll(List<Product> products) {
        return productJpaRepository.saveAll(products);
    }

    // UPDATE
    @Override
    public boolean incrementLikeCount(Long productId) {
        return productJpaRepository.incrementLikeCount(productId) > 0;
    }

    @Override
    public boolean decrementLikeCount(Long productId) {
        return productJpaRepository.decrementLikeCount(productId) > 0;
    }

    @Override
    public List<Product> findAllById(List<Long> productIds) {
        return productJpaRepository.findAllById(productIds);
    }

    @Override
    public void updateLikeCount(Long productId, long count) {
        jpaQueryFactory
                .update(product)
                .set(product.likeCount, product.likeCount.add(count)) // ← delta 더하기
                .where(product.id.eq(productId))
                .execute();
    }

    @Override
    public Long getLikeCount(Long productId) {
        return jpaQueryFactory
                .select(product.likeCount)
                .from(product)
                .where(product.id.eq(productId))
                .fetchOne();
    }

    private OrderSpecifier<?>[] productSort(ProductSortType s) {
        return switch (s) {
            case LATEST ->
                    new OrderSpecifier<?>[]{ product.createdAt.desc(), product.id.desc() };
            case PRICE_ASC ->
                    new OrderSpecifier<?>[]{ product.price.value.asc(), product.id.asc() };
            case LIKES_DESC ->
                    new OrderSpecifier<?>[]{ product.likeCount.desc(), product.id.desc() };
        };
    }
}
