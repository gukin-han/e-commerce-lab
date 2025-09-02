package com.loopers.domain.product;

import com.loopers.domain.BaseEntity;
import com.loopers.common.error.CoreException;
import com.loopers.common.error.ErrorType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "products")
@Entity
public class Product extends BaseEntity {

    @Embedded
    private Stock stock;

    @Column(nullable=false)
    private long likeCount;

    @Enumerated(value = EnumType.STRING)
    private ProductStatus status;

    private String name;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "price"))
    private Money price;

    private Long brandId;

    @Builder
    private Product(Stock stock, long likeCount, ProductStatus status, String name, Money price, Long brandId) {
        this.stock = stock;
        this.likeCount = likeCount;
        this.status = status;
        this.name = name;
        this.price = price;
        this.brandId = brandId;
    }

    public Product(long stockQuantity) {

        this.stock = Stock.of(stockQuantity);
        this.likeCount = 0;
        this.status = ProductStatus.ACTIVE;
    }

    public static Product create(Stock stock, String name, Money price, Long brandId) {
        return Product.builder()
                .stock(stock)
                .name(name)
                .price(price)
                .brandId(brandId)
                .likeCount(0)
                .status(ProductStatus.ACTIVE)
                .build();
    }

    public void decreaseStock(long quantity) {

        this.canDecrease();
        Stock decreasedStock = stock.decrease(quantity);
        if (decreasedStock.isSoldOut()) {
            this.status = ProductStatus.SOLD_OUT;
        }
        this.stock = decreasedStock;
    }

    public void increaseStock(long quantity) {
        this.stock = stock.increase(quantity);
    }

    private void canDecrease() {
        if (this.status == ProductStatus.SOLD_OUT) {
            throw new CoreException(ErrorType.CONFLICT, "재고 수량이 없습니다.");
        }

        if (this.status == ProductStatus.STOPPED) {
            throw new CoreException(ErrorType.CONFLICT, "판매가 중단된 상품입니다.");
        }
    }

    public void increaseLikeCount() {
        this.likeCount++;
    }

    public void decreaseLikeCount() {
        if (this.likeCount - 1 < 0) {
            throw new CoreException(ErrorType.CONFLICT, "상품의 좋아요 수는 0보다 작을 수 없습니다.");
        }
        this.likeCount--;
    }
}
