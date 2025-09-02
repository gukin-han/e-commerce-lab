package com.loopers.interfaces.api.like;

import com.loopers.application.like.ProductLikeFacade;
import com.loopers.application.like.ProductLikeCommand;
import com.loopers.application.like.ProductLikeResult;
import com.loopers.interfaces.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import static com.loopers.interfaces.api.ApiHeader.LOGIN_ID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/products/likes")
public class ProductLikeV1Controller implements ProductLikeV1ApiSpec{

    private final ProductLikeFacade productLikeFacade;

    @Override
    @PostMapping("/{productId}")
    public ApiResponse<ProductLikeV1Response.Like> like(
            @RequestHeader(LOGIN_ID) String loginId,
            @PathVariable(value = "productId") Long productId
    ) {
        ProductLikeResult.Like result = productLikeFacade.like(ProductLikeCommand.Like.of(loginId, productId));
        return ApiResponse.success(ProductLikeV1Response.Like.fromResult(result));
    }

    @Override
    @DeleteMapping("/{productId}")
    public ApiResponse<ProductLikeV1Response.Unlike> unlike(
            @RequestHeader(LOGIN_ID) String loginId,
            @PathVariable(value = "productId") Long productId
    ) {
        ProductLikeResult.Unlike result = productLikeFacade.unlike(ProductLikeCommand.Unlike.of(loginId, productId));
        return ApiResponse.success(ProductLikeV1Response.Unlike.fromResult(result));
    }
}
