package com.loopers.interfaces.api.product;

import com.loopers.application.common.dto.PagedResult;
import com.loopers.application.product.dto.ProductDetailView;
import com.loopers.application.product.dto.ProductSortType;
import com.loopers.application.product.dto.ProductSummaryView;
import com.loopers.domain.product.ProductStatus;
import com.loopers.domain.product.Stock;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

public class ProductV1Dto {

    @Data
    @Builder
    @AllArgsConstructor
    public static class GetProductsResponse {

        private final List<ProductSummaryResponse> items;
        private final int currentPage;
        private final int totalPages;
        private final long totalItems;
        private final boolean hasNext;

        public static GetProductsResponse of(PagedResult<ProductSummaryView> pagedProducts) {
            return GetProductsResponse.builder()
                    .items(pagedProducts.getItems().stream()
                            .map(ProductSummaryResponse::of)
                            .toList())
                    .currentPage(pagedProducts.getCurrentPage())
                    .totalPages(pagedProducts.getTotalPages())
                    .totalItems(pagedProducts.getTotalItems())
                    .hasNext(pagedProducts.isHasNext())
                    .build();
        }

        @Data
        @Builder
        @AllArgsConstructor
        public static class ProductSummaryResponse {
            // Product
            private final Long productId;

            private final Stock stock;

            private final Long likeCount;

            private final ProductStatus status;

            private final String productName;

            private final Double price;

            // Brand
            private final Long brandId;

            private final String brandName;

            public static ProductSummaryResponse of(ProductSummaryView productSummaryView) {
                return ProductSummaryResponse.builder()
                        .productId(productSummaryView.getProductId())
                        .stock(productSummaryView.getStock())
                        .likeCount(productSummaryView.getLikeCount())
                        .status(productSummaryView.getStatus())
                        .productName(productSummaryView.getProductName())
                        .price(productSummaryView.getPrice().getValue().doubleValue())
                        .brandId(productSummaryView.getBrandId())
                        .brandName(productSummaryView.getBrandName())
                        .build();
            }
        }
    }

    @Data
    public static class GetProductsRequest {

        @Schema(description = "필터 적용할 브랜드의 ID")
        private Long brandId;

        @Schema(description = "정렬 타입")
        private ProductSortType sortType;

        @Schema(description = "페이지 넘버 (0-base)", defaultValue = "0", minimum = "0")
        private Integer page;

        @Schema(description = "페이지 사이즈", defaultValue = "20", minimum = "1", maximum = "100")
        private Integer size;
    }

    @Data
    @Builder
    @AllArgsConstructor
    public static class GetProductByIdResponse {

        // Product
        private final Long productId;

        private final long stock;

        private final long likeCount;

        private final ProductStatus status;

        private final String productName;

        private final Double price;

        // Brand
        private final Long brandId;

        private final String brandName;

        public static GetProductByIdResponse of(ProductDetailView view) {
            return GetProductByIdResponse.builder()
                    .productId(view.getProductId())
                    .stock(view.getStock().getQuantity())
                    .likeCount(view.getLikeCount())
                    .status(view.getStatus())
                    .productName(view.getProductName())
                    .price(view.getPrice().getValue().doubleValue())
                    .brandId(view.getBrandId())
                    .brandName(view.getBrandName())
                    .build();
        }
    }

    @Data
    public static class GetProductByIdRequest {

        @Schema(name = "productId", description = "조회할 상품의 ID")
        private Long productId;
    }
}
