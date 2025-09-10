package com.loopers.application.rank;

import com.loopers.domain.brand.Brand;
import com.loopers.domain.product.Product;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class RankResult {
    public record GetRankings(List<RankingProduct> products) {
        public static GetRankings from(List<Long> sortedProductIds, List<Product> products, List<Brand> brands, Map<Long, Double> scoresMap) {
            Map<Long, Product> productMap = products.stream().collect(Collectors.toMap(Product::getId, Function.identity()));
            Map<Long, Brand> brandMap = brands.stream().collect(Collectors.toMap(Brand::getId, Function.identity()));

            List<RankingProduct> rankingProducts = sortedProductIds.stream()
                    .map(productId -> {
                        Product product = productMap.get(productId);
                        Brand brand = brandMap.get(product.getBrandId());
                        Double score = scoresMap.get(productId);
                        return RankingProduct.of(product, brand, score);
                    })
                    .toList();

            return new GetRankings(rankingProducts);
        }
    }

    public record RankingProduct(
            Long productId,
            String productName,
            String brandName,
            double score
    ) {
        public static RankingProduct of(Product product, Brand brand, Double score) {
            return new RankingProduct(
                    product.getId(),
                    product.getName(),
                    brand.getName(),
                    score
            );
        }
    }
}
