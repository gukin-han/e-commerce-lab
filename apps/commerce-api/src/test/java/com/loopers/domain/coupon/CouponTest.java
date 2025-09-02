package com.loopers.domain.coupon;

import com.loopers.domain.product.Money;

import java.math.BigDecimal;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CouponTest {

    @DisplayName("쿠폰 생성시")
    @Nested
    class create {

        @Test
        @DisplayName("정상적으로 정액 쿠폰을 생성한다.")
        void createFixedAmountCoupon() {
            // given
            CouponType type = CouponType.FIXED_AMOUNT;
            Long userId = 1L;
            Money amount = Money.of(new BigDecimal(1000));

            // when
            Coupon coupon = Coupon.create(type, userId, amount, null);

            // then
            Assertions.assertAll(
                    () -> assertNotNull(coupon),
                    () -> assertEquals(type, coupon.getType()),
                    () -> assertEquals(userId, coupon.getUserId()),
                    () -> assertEquals(amount, coupon.getAmount()),
                    () -> assertNull(coupon.getDiscountRate())
            );

        }

        @Test
        @DisplayName("정상적으로 정률 쿠폰을 생성한다")
        void createPercentageCoupon_withZeroOrNegativePercent() {
            // given
            CouponType type = CouponType.PERCENTAGE;
            Long userId = 1L;
            Percent percent = Percent.of(0.1d);

            // when
            Coupon coupon = Coupon.create(type, userId, null, percent);

            // then
            assertNotNull(coupon);
            assertEquals(type, coupon.getType());
            assertEquals(userId, coupon.getUserId());
            assertNull(coupon.getAmount());
            assertEquals(percent, coupon.getDiscountRate());
        }

        @Test
        @DisplayName("쿠폰 타입이 null이면 예외가 발생한다.")
        void throwsException_whenTypeIsNull() {
            // given
            Long userId = 1L;
            CouponType type = null;

            // when
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> Coupon.create(null, userId, null, null));

            // then
            assertEquals("쿠폰 타입과 유저아이디는 필수 입력값입니다.", exception.getMessage());
        }

        @Test
        @DisplayName("유저 아이디가 null이면 예외가 발생한다.")
        void throwsException_whenUserIdIsNull() {
            // given
            CouponType type = CouponType.FIXED_AMOUNT;
            Money amount = Money.of(new BigDecimal(1000));
            Long userId = null;


            // when
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> Coupon.create(type, userId, amount, null));

            // then
            assertEquals("쿠폰 타입과 유저아이디는 필수 입력값입니다.", exception.getMessage());
        }

        @Test
        @DisplayName("정액 쿠폰의 할인금액이 null이면 예외가 발생한다.")
        void throwsException_whenFixedAmountCouponAmountIsNull() {
            // given
            CouponType type = CouponType.FIXED_AMOUNT;
            Long userId = 1L;

            // when
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> Coupon.create(type, userId, null, null));

            // then
            assertEquals("정액 쿠폰은 할인금액이 필수 입니다.", exception.getMessage());
        }

        @Test
        @DisplayName("정률 쿠폰의 할인율이 null이면 예외가 발생한다.")
        void throwsException_whenPercentageCouponPercentIsNull() {
            // given
            CouponType type = CouponType.PERCENTAGE;
            Long userId = 1L;

            // when
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> Coupon.create(type, userId, null, null));

            // then
            assertEquals("정률 쿠폰은 할인율이 필수 입니다.", exception.getMessage());
        }

        @Test
        @DisplayName("정률 쿠폰의 할인율이 1초과인 경우 예외가 발생한다.")
        void throwsException_whenPercentageCouponPercentIsLessThanAndEqualsZero() {
            // given
            CouponType type = CouponType.PERCENTAGE;
            Long userId = 1L;
            Double percent = 1.1;

            // when
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> Percent.of(percent));

            // then
            assertEquals("할인율은 0보다 크고 1 이하이어야 합니다.", exception.getMessage());
        }

        @Test
        @DisplayName("정액 쿠폰의 할인금액이 0 이하 음수이면 예외가 발생한다.")
        void throwsException_whenFixedAmountCouponAmountIsLessThanAndEqualsZero() {
            // given
            CouponType type = CouponType.FIXED_AMOUNT;
            Long userId = 1L;
            long amount = -10;

            // when
            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> Coupon.create(type, userId, Money.of(amount), null));

            // then
            assertEquals("할인금액은 양수여야 합니다.", exception.getMessage());
        }
    }
}
