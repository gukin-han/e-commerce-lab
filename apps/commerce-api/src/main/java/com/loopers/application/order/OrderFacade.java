package com.loopers.application.order;

import com.loopers.domain.coupon.CouponService;
import com.loopers.domain.order.Order;
import com.loopers.domain.order.OrderEvent;
import com.loopers.domain.order.OrderItemRepository;
import com.loopers.domain.order.OrderService;
import com.loopers.domain.product.*;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class OrderFacade {

    private final ProductService productService;
    private final OrderService orderService;
    private final CouponService couponService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public OrderResult.Create create(OrderCommand.Create command) {
        Map<Long, Stock> productIdToStockMap = this.getProductIdStockMap(command.items());

        // 1. 상품 재고 차감
        productService.deductStocks(productIdToStockMap);

        // 2. 쿠폰 적용 및 할인 계산
        Money totalPrice = productService.calculateTotalPrice(productIdToStockMap);
        Money discountPrice = couponService.apply(command.couponId(), command.userId(), totalPrice);

        // 3. 주문 생성
        Order order = orderService.create(command.userId(), productIdToStockMap, totalPrice, discountPrice);

        // 4. 주문 생성 이벤트 발행
        eventPublisher.publishEvent(new OrderEvent.Created(order.getId(), order.getUserId()));
        return OrderResult.Create.success(order.getId());
    }

    private Map<Long, Stock> getProductIdStockMap(List<OrderCommand.CartItem> items) {
        return items.stream()
                .collect(Collectors.toMap(
                        OrderCommand.CartItem::productId,
                        it -> Stock.of(it.quantity()))
                );
    }

    @Transactional
    public void completePayment(Long orderId) {
        orderService.completePayment(orderId);
    }

    @Transactional
    public void handlePaymentFailure(Long orderId) {
        orderService.handlePaymentFailure(orderId);
        couponService.restoreCoupon(orderId);
    }
}
