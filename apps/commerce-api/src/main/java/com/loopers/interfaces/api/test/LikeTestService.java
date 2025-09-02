package com.loopers.interfaces.api.test;

import com.loopers.domain.like.ProductLikeEvent;
import com.loopers.domain.like.ProductLikeRepository;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.redis.core.StringRedisTemplate;

@Component
@RequiredArgsConstructor
public class LikeTestService {

    private final UserService userService;
    private final ProductLikeRepository productLikeRepository;
    private final ProductRepository productRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final StringRedisTemplate redis;

    @Transactional
    public void like(String loginId, Long productId, String mode) {
        // 0) 유저 찾기
//        User user = userService.getByLoginId(loginId);
        Long userId = syntheticUserId(loginId);

        // 1) 좋아요 레코드 멱등 insert
        boolean isInserted = productLikeRepository.insertIgnoreDuplicateKey(userId, productId);
        if (!isInserted) return; // 이미 눌렀으면 끝

        // 2) 모드별 집계 경로 (SYNC | ASYNC_EVENT | SCHEDULED_REDIS)
        switch (mode) {
            case "SYNC" -> productRepository.incrementLikeCount(productId);
            case "ASYNC_EVENT" -> eventPublisher.publishEvent(new ProductLikeEvent.Added(productId, null, null));
            case "SCHEDULED_REDIS" -> {
                redis.opsForValue().increment("like:count:" + productId);
                redis.opsForSet().add("like:dirty", String.valueOf(productId));
            }
        }
    }

    public Long getLikeCount(Long productId) {
        return productRepository.getLikeCount(productId);
    }

    static long syntheticUserId(String loginId) {
        try {
            var md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(loginId.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            // 앞 8바이트를 long으로
            long v =
                    ((long)(digest[0] & 0xff) << 56) |
                            ((long)(digest[1] & 0xff) << 48) |
                            ((long)(digest[2] & 0xff) << 40) |
                            ((long)(digest[3] & 0xff) << 32) |
                            ((long)(digest[4] & 0xff) << 24) |
                            ((long)(digest[5] & 0xff) << 16) |
                            ((long)(digest[6] & 0xff) <<  8) |
                            ((long)(digest[7] & 0xff));
            return v & 0x7fffffffffffffffL; // 양수로
        } catch (Exception e) { throw new RuntimeException(e); }
    }
}
