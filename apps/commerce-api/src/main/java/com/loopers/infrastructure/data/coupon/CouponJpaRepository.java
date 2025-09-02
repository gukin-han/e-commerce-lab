package com.loopers.infrastructure.data.coupon;

import com.loopers.domain.coupon.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CouponJpaRepository extends JpaRepository<Coupon, Long> {
    Optional<Coupon> findByIdAndUserId(Long couponId, Long userId);

    Optional<Coupon> findByOrderId(Long orderId);
}
