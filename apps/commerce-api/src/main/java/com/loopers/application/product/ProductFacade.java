package com.loopers.application.product;

import com.fasterxml.jackson.core.type.TypeReference;
import com.loopers.application.common.dto.PagedResult;
import com.loopers.application.product.dto.ProductDetailQuery;
import com.loopers.application.product.dto.ProductDetailView;
import com.loopers.application.product.dto.ProductPageQuery;
import com.loopers.application.product.dto.ProductSummaryView;
import com.loopers.domain.brand.Brand;
import com.loopers.domain.brand.BrandService;
import com.loopers.domain.product.*;
import com.loopers.common.cache.CacheRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ProductFacade {

    private final ProductService productService;
    private final BrandService brandService;
    private final ProductRepository productRepository;
    private final CacheRepository cacheRepository;

    private static final Duration TTL_DETAIL = Duration.ofMinutes(10);
    private static final Duration TTL_LIST = Duration.ofSeconds(45);

    @Transactional(readOnly = true)
    public ProductDetailView getProductDetail(ProductDetailQuery query) {
        long pid = query.getProductId();
        String key = "v1:prod:detail:" + pid;

        return cacheRepository.cacheAside(
                key,
                () -> {
                    Product product = productService.findByProductId(pid);
                    Brand brand = brandService.findByBrandId(product.getBrandId());
                    return ProductDetailView.create(product, brand);
                },
                new TypeReference<>() {},
                TTL_DETAIL
        );
    }

    @Transactional(readOnly = true)
    public PagedResult<ProductSummaryView> getPagedProducts(ProductPageQuery query) {
        String brandPart = query.getBrandId() == null ? "null" : String.valueOf(query.getBrandId());
        String key = String.format(
                "v1:prod:list:brand=%s:sort=%s:page=%d:size=%d",
                brandPart, query.getSortType(), query.getPage(), query.getSize()
        );

        TypeReference<PagedResult<ProductSummaryView>> typeRef = new TypeReference<>() {};

        return cacheRepository.cacheAside(
                key,
                () -> {
                    Long brandId = query.getBrandId() == null ? null : query.getBrandId();

                    List<ProductSummaryView> views = productService.findProducts(
                            brandId,
                            query.getPage(),
                            query.getSize(),
                            query.getSortType()
                    ).stream().map(ProductSummaryView::from).collect(Collectors.toList());

                    long total = productRepository.getTotalCountByBrandId(brandId);

                    return PagedResult.of(views, query.getPage(), total, query.getSize());
                },
                typeRef,
                TTL_LIST
        );
    }

    public void increaseLikeCount(Long productId) {
        productRepository.incrementLikeCount(productId);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void decreaseLikeCount(Long productId) {
        productRepository.decrementLikeCount(productId);
    }
}
