package com.loopers.domain.order;

import com.loopers.common.error.CoreException;
import com.loopers.common.error.ErrorType;
import com.loopers.domain.product.Money;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.product.Stock;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductService productService;

    @Transactional
    public Order create(Long userId, Map<Long, Stock> productIdToStockMap, Money totalPrice, Money discountAmount) {
        Order order = orderRepository.save(Order.of(userId, totalPrice, discountAmount, OrderStatus.CREATED));

        List<OrderItem> orderItems = productIdToStockMap.entrySet().stream()
                .map(entry -> OrderItem.of(order.getId(), entry.getKey(), entry.getValue().getQuantity()))
                .toList();

        orderItemRepository.saveAll(orderItems);
        return order;
    }

    @Transactional(readOnly = true)
    public Order get(Long orderId) {
        return orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "주문을 찾을 수 없습니다. orderId: " + orderId));
    }

    @Transactional
    public void completePayment(Long orderId) {
        Order order = this.require(orderId);
        order.completePayment();
    }

    @Transactional
    public void handlePaymentFailure(Long orderId) {
        Order order = this.require(orderId);
        order.failPayment();

        List<OrderItem> orderItems = orderItemRepository.findOrderItemsByOrderId(orderId);
        productService.restoreStocks(orderItems.stream().collect(Collectors.toMap(OrderItem::getProductId, item -> Stock.of(item.getQuantity()))));;
    }

    private Order require(Long orderId) {
        return orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "주문을 찾을 수 없습니다. orderId: " + orderId));
    }
}
