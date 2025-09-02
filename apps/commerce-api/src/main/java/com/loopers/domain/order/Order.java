package com.loopers.domain.order;

import com.loopers.domain.BaseEntity;
import com.loopers.domain.product.Money;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "orders")
public class Order extends BaseEntity {

    private Long userId;

    private Long couponId;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "total_price"))
    private Money totalPrice;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "discount_amount"))
    private Money discountAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Version
    private Long version;


    @Builder
    private Order(Long userId, Money totalPrice, Money discountAmount, OrderStatus status, Long couponId) {
        this.userId = userId;
        this.totalPrice = totalPrice;
        this.discountAmount = discountAmount;
        this.status = status;
        this.couponId = couponId;
    }

    public static Order of(Long userId, Money totalPrice, Money discountAmount, OrderStatus status) {
        return Order.builder()
                .userId(userId)
                .totalPrice(totalPrice)
                .status(status)
                .build();
    }

    public void completePayment() {
        this.status = OrderStatus.PAID;
    }

    public void failPayment() {
        this.status = OrderStatus.CANCELED;
    }
}
