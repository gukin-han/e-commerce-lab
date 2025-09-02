package com.loopers.interfaces.api.like;

import com.loopers.interfaces.api.ApiResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Product Like V1 API", description = "상품 좋아요 관리 API")
public interface ProductLikeV1ApiSpec {

    ApiResponse<ProductLikeV1Response.Like> like(
            @Schema(name = "X-USER-ID", description = "로그인 유저의 ID")
            String loginId,
            @Schema(name = "productId", description = "좋아요를 누를 상품의 ID")
            Long productId
    );

    ApiResponse<ProductLikeV1Response.Unlike> unlike(
            @Schema(name = "X-USER-ID", description = "로그인 유저의 ID")
            String loginId,
            @Schema(name = "productId", description = "좋아요를 취소할 상품의 ID")
            Long productId
    );

}
