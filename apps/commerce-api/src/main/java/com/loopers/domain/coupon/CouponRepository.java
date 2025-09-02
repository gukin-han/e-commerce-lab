package com.loopers.domain.coupon;

import java.util.Optional;

public interface CouponRepository {
    Optional<Coupon> findByIdAndUserId(Long couponId, Long userId);

    Coupon save(Coupon coupon);

    Optional<Coupon> findByOrderId(Long orderId);
}
