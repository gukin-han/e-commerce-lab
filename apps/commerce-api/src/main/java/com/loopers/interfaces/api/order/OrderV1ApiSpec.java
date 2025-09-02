package com.loopers.interfaces.api.order;

import com.loopers.interfaces.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Order V1 API", description = "주문 관리 API")
public interface OrderV1ApiSpec {

    @Operation(
            summary = "주문 생성",
            description = "새로운 주문을 생성합니다."
    )
    ApiResponse<OrderV1Response.Create> create(
            @Schema(description = "X-USER-ID")
            String loginId,
            @Schema(description = "주문 생성 요청 정보")
            OrderV1Request.Create request
    );
}
