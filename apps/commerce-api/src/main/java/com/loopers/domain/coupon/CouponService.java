package com.loopers.domain.coupon;

import com.loopers.common.error.CoreException;
import com.loopers.common.error.ErrorType;
import com.loopers.domain.product.Money;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class CouponService {

    private final CouponRepository couponRepository;

    @Transactional
    public Money apply(Long couponId, Long userId, Money totalPrice) {
        if (couponId == null) {
            return Money.ZERO;
        }

        Coupon coupon = couponRepository.findByIdAndUserId(couponId, userId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "쿠폰을 찾을 수 없습니다."));

        if (coupon.isUsed()) {
            throw new CoreException(ErrorType.CONFLICT, "쿠폰은 한 번만 사용할 수 있습니다.");
        }

        coupon.use();
        CouponDiscountCalculator calculator = new CouponDiscountCalculator();
        return calculator.calculateDiscountAmount(coupon, totalPrice);
    }

    public void restoreCoupon(Long orderId) {
        Coupon coupon = couponRepository.findByOrderId(orderId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "쿠폰을 찾을 수 없습니다."));
        coupon.restore();
    }
}
