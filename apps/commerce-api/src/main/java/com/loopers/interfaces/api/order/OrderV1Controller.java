package com.loopers.interfaces.api.order;

import com.loopers.application.order.OrderFacade;
import com.loopers.application.order.OrderResult;
import com.loopers.interfaces.api.ApiHeader;
import com.loopers.interfaces.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/orders")
public class OrderV1Controller implements OrderV1ApiSpec {

    private final OrderFacade orderFacade;

    @PostMapping
    @Override
    public ApiResponse<OrderV1Response.Create> create(
            @RequestHeader(ApiHeader.LOGIN_ID) String loginId,
            @RequestBody OrderV1Request.Create request
    ) {

        OrderResult.Create result = orderFacade.create(request.toCommand());
        return ApiResponse.success(OrderV1Response.Create.fromResult(result));
    }
}
