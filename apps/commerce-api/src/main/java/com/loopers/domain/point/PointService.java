package com.loopers.domain.point;

import com.loopers.domain.product.Money;
import com.loopers.domain.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PointService {

    private final PointRepository pointRepository;

    @Transactional
    public Point getPointByUserId(String userId) {
        return pointRepository.findByLoginId(userId).orElse(null);
    }

    @Transactional
    public Point initializePoints(User user) {
        return pointRepository.save(Point.create(user, Money.ZERO));
    }

    public Point save(Point point) {
        return pointRepository.save(point);
    }
}
