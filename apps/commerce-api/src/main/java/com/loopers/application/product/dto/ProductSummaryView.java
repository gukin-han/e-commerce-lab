package com.loopers.application.product.dto;

import com.loopers.domain.brand.Brand;
import com.loopers.domain.product.*;
import lombok.Builder;
import lombok.Data;

@Data
public class ProductSummaryView {

    // Product
    private final Long productId;

    private final Stock stock;

    private final long likeCount;

    private final ProductStatus status;

    private final String productName;

    private final Money price;

    // Brand
    private final Long brandId;

    private final String brandName;

    @Builder
    private ProductSummaryView(Long productId, Stock stock, long likeCount, ProductStatus status, String productName, Money price, Long brandId, String brandName) {
        this.productId = productId;
        this.stock = stock;
        this.likeCount = likeCount;
        this.status = status;
        this.productName = productName;
        this.price = price;
        this.brandId = brandId;
        this.brandName = brandName;
    }

    public static ProductSummaryView of(Product product, Brand brand) {
        return ProductSummaryView.builder()
                .productId(product.getId())
                .stock(product.getStock())
                .likeCount(product.getLikeCount())
                .status(product.getStatus())
                .productName(product.getName())
                .price(product.getPrice())
                .brandId(brand.getId())
                .brandName(brand.getName())
                .build();
    }

    public static ProductSummaryView from(ProductDetail productDetail) {
        return ProductSummaryView.builder()
                .productId(productDetail.getProductId())
                .productName(productDetail.getProductName())
                .price(productDetail.getPrice())
                .stock(productDetail.getStock())
                .likeCount(productDetail.getLikeCount())
                .brandId(productDetail.getBrandId())
                .brandName(productDetail.getBrandName())
                .build();
    }
}
