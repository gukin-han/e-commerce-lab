package com.loopers.domain.product;

import com.loopers.common.error.CoreException;
import com.loopers.common.error.ErrorType;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

@Getter
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Stock {

    @Column(name = "stock_quantity")
    private long quantity;

    @Builder
    private Stock(long quantity) {

        if (quantity < 0) {
            throw new CoreException(ErrorType.BAD_REQUEST, "수량은 음수 값으로 등록할 수 없습니다.");
        }

        this.quantity = quantity;
    }

    public static Stock of(long quantity) {
        return Stock.builder()
                .quantity(quantity)
                .build();
    }

    public Stock decrease(long quantity) {
        if (quantity <= 0) {
            throw new CoreException(ErrorType.BAD_REQUEST, "0 보다 작거나 같은 값으로 재고를 차감할 수 없습니다.");
        }

        long remaining = this.quantity - quantity;
        if (remaining < 0) {
            throw new CoreException(ErrorType.CONFLICT, "수량이 부족합니다.");
        }
        return new Stock(remaining);
    }

    public Stock increase(long quantity) {
        if (quantity <= 0) {
            throw new CoreException(ErrorType.BAD_REQUEST, "0 보다 작거나 같은 값으로 재고를 증가시킬 수 없습니다.");
        }
        return new Stock(this.quantity + quantity);
    }

    public boolean isSoldOut() {
        return quantity == 0;
    }
}
