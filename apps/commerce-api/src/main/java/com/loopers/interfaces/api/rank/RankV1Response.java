package com.loopers.interfaces.api.rank;

import com.loopers.application.rank.RankResult;
import lombok.Builder;

import java.util.List;
import java.util.stream.Collectors;

public class RankV1Response {

    @Builder
    public record GetRankings(
            List<RankingProduct> products
    ) {
        public static GetRankings from(RankResult.GetRankings result) {
            List<RankingProduct> rankingProducts = result.products().stream()
                    .map(RankingProduct::from)
                    .collect(Collectors.toList());
            return new GetRankings(rankingProducts);
        }
    }

    @Builder
    public record RankingProduct(
            Long productId,
            String productName,
            String brandName,
            double score
    ) {
        public static RankingProduct from(RankResult.RankingProduct result) {
            return new RankingProduct(
                    result.productId(),
                    result.productName(),
                    result.brandName(),
                    result.score()
            );
        }
    }
}
