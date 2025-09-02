package com.loopers.interfaces.api.test;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

import static com.loopers.interfaces.api.ApiHeader.LOGIN_ID;

@RequiredArgsConstructor
@RestController
public class LikeTestController {

    private final LikeTestService service;

    @PostMapping("/api/v1/products/{productId}/likes/{mode}")
    public void like(
            @RequestHeader(LOGIN_ID) String loginId,
            @PathVariable Long productId,
            @PathVariable String mode
    ) {
        service.like(loginId, productId, mode);
    }

    @GetMapping("/api/v1/products/{productId}/likes/count")
    public CountResult getLikeCount(@PathVariable Long productId) {
        Long likeCount = service.getLikeCount(productId);
        return new CountResult(likeCount, Instant.now().toString());
    }

    public record CountResult(long count, String asOf) {}
}
