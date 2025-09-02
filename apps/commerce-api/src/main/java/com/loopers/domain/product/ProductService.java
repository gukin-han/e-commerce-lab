package com.loopers.domain.product;

import com.loopers.application.product.dto.ProductSortType;
import com.loopers.common.error.CoreException;
import com.loopers.common.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    @Transactional(readOnly = true)
    public Product findByProductId(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "조회할 수 없는 상품입니다."));
    }

    @Transactional(readOnly = true)
    public List<ProductDetail> findProducts(Long brandId, int page, int size, ProductSortType sortType) {
        return productRepository.findPagedProductDetails(brandId, page, size, sortType);
    }

    public Product save(Product product) {
        return productRepository.save(product);
    }

    @Transactional
    public void deductStocks(Map<Long, Stock> productIdToStockMap) {
        List<Long> productIds = productIdToStockMap.keySet().stream()
                .toList();

        List<Product> products = productRepository.findAllByIdsWithPessimisticLock(productIds);

        if (products.size() != productIds.size()) {
            throw new IllegalArgumentException("조회결과, 존재하지 않는 상품이 있습니다.");
        }

        for (Product product : products) {
            Long productId = product.getId();
            Stock stock = productIdToStockMap.get(productId);
            product.decreaseStock(stock.getQuantity());
        }
    }

    @Transactional
    public void restoreStocks(Map<Long, Stock> productIdToStockMap) {
        List<Long> productIds = productIdToStockMap.keySet().stream()
                .toList();

        List<Product> products = productRepository.findAllByIdsWithPessimisticLock(productIds);

        if (products.size() != productIds.size()) {
            throw new IllegalArgumentException("조회결과, 존재하지 않는 상품이 있습니다.");
        }

        for (Product product : products) {
            Long productId = product.getId();
            Stock stock = productIdToStockMap.get(productId);
            product.increaseStock(stock.getQuantity());
        }
    }

    @Transactional(readOnly = true)
    public Money calculateTotalPrice(Map<Long, Stock> productIdToStockMap) {
        List<Long> productIds = productIdToStockMap.keySet().stream()
                .toList();
        List<Product> products = productRepository.findAllById(productIds);

        Money totalPrice = Money.of(0L);
        for (Product product : products) {
            Money unitPrice = product.getPrice();
            Stock stock = productIdToStockMap.get(product.getId());
            totalPrice = totalPrice.add(unitPrice.multiply(stock.getQuantity()));
        }
        return totalPrice;
    }
}
