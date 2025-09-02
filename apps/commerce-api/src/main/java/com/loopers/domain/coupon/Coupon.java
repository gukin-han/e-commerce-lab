package com.loopers.domain.coupon;

import com.loopers.domain.BaseEntity;
import com.loopers.domain.product.Money;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "coupons")
@Entity
public class Coupon extends BaseEntity {

    @Enumerated(value = EnumType.STRING)
    private CouponType type;

    private Long userId;

    private Long orderId;

    @Embedded
    private Money amount;

    @Embedded
    private Percent discountRate;

    @Enumerated(value = EnumType.STRING)
    private CouponStatus status;

    @Version
    private Long version;

    @Builder
    private Coupon(CouponType type, Long userId, Money amount, Percent discountRate, CouponStatus status) {
        if (type == null || userId == null) {
            throw new IllegalArgumentException("쿠폰 타입과 유저아이디는 필수 입력값입니다.");
        }

        if (CouponType.FIXED_AMOUNT.equals(type)) {
            this.validateFixedTypeCoupon(amount);
        }

        if (CouponType.PERCENTAGE.equals(type)) {
            this.validatePercentageCoupon(discountRate);
        }

        this.type = type;
        this.userId = userId;
        this.amount = amount;
        this.discountRate = discountRate;
    }

    private void validatePercentageCoupon(Percent discountRate) {
        if (discountRate == null) {
            throw new IllegalArgumentException("정률 쿠폰은 할인율이 필수 입니다.");
        }
    }

    private void validateFixedTypeCoupon(Money amount) {
        if (amount == null) {
            throw new IllegalArgumentException("정액 쿠폰은 할인금액이 필수 입니다.");
        }
        if (!amount.isPositive()) {
            throw new IllegalArgumentException("할인금액은 양수여야 합니다.");
        }
    }

    public static Coupon create(CouponType type, Long userId, Money amount, Percent discountRate) {
        return Coupon.builder()
                .type(type)
                .userId(userId)
                .amount(amount)
                .discountRate(discountRate)
                .build();
    }

    public void use() {
        this.status = CouponStatus.USED;
    }

    public boolean isUsed() {
        return this.status == CouponStatus.USED;
    }

}

enum CouponStatus {
    AVAILABLE, USED, CANCELED
}
