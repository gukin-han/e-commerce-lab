package com.loopers.application.rank;

import com.loopers.domain.brand.Brand;
import com.loopers.domain.brand.BrandService;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.rank.RankInMemoryRepository;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RankFacade {

    private final RankInMemoryRepository rankInMemoryRepository;
    private final ProductService productService;
    private final BrandService brandService;

    public RankResult.GetRankings getRankings(RankCommand.GetRankings command) {
        // 1. paging
        String key = "ranking:all:" + command.date().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long start = (long) (command.page() - 1) * command.size();
        long end = start + command.size() - 1;

        // 2. top k 상품 리스트 조회
        Set<TypedTuple<String>> productIdsWithScores = rankInMemoryRepository.findTopRankings(key, start, end);
        if (productIdsWithScores == null || productIdsWithScores.isEmpty()) {
            return new RankResult.GetRankings(Collections.emptyList());
        }
        List<Long> sortedProductIds = productIdsWithScores.stream()
                .map(TypedTuple::getValue)
                .filter(Objects::nonNull)
                .map(Long::parseLong)
                .toList();

        Map<Long, Double> scoresMap = productIdsWithScores.stream()
                .collect(Collectors.toMap(tuple -> Long.parseLong(tuple.getValue()), TypedTuple::getScore));

        // 3. 상품, 브랜드 정보 조회
        List<Product> products = productService.findAllByIds(sortedProductIds);
        List<Long> brandIds = products.stream().map(Product::getBrandId).distinct().toList();
        List<Brand> brands = brandService.findAllByIds(brandIds);

        return RankResult.GetRankings.from(sortedProductIds, products, brands, scoresMap);
    }
}
