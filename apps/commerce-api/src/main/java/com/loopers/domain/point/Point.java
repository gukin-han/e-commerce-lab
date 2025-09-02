package com.loopers.domain.point;

import com.loopers.domain.BaseEntity;
import com.loopers.domain.product.Money;
import com.loopers.domain.user.User;
import com.loopers.common.error.CoreException;
import com.loopers.common.error.ErrorType;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Table(name = "points")
@Entity
public class Point extends BaseEntity {

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "balance"))
    private Money balance;

    private String loginId;

    private Long userId;

    @Builder
    private Point(Money balance, String loginId, Long userId) {
        if (loginId == null || loginId.isEmpty()) {
            throw new CoreException(ErrorType.BAD_REQUEST, "유저 아이디는 필수 입력 항목입니다.");
        }

        if (balance.isNegative()) {
            throw new CoreException(ErrorType.BAD_REQUEST, "포인트 잔액은 0 이상이어야 합니다.");
        }

        this.userId = userId;
        this.balance = balance;
        this.loginId = loginId;
    }

    public static Point create(User user, Money balance) {
        return Point.builder()
                .balance(balance)
                .userId(user.getId())
                .loginId(user.getLoginId())
                .build();
    }

    public void add(Money amount) {
        if (!amount.isPositive()) {
            throw new CoreException(ErrorType.BAD_REQUEST, "충전 금액은 0보다 커야 합니다.");
        }

        this.balance = this.balance.add(amount);
    }

    public void deduct(Money amount) {
        if (this.balance.isLessThan(amount)) {
            throw new IllegalStateException("포인트가 부족합니다.");
        }
        this.balance = this.balance.subtract(amount);
    }
}
