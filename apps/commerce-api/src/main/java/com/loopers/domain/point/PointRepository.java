package com.loopers.domain.point;

import java.util.Optional;

public interface PointRepository {

    Optional<Point> findByLoginId(String loginId);

    Point save(Point point);

    Optional<Point> findByUserIdForUpdate(Long userId);

    Optional<Point> findByUserId(Long userId);
}
