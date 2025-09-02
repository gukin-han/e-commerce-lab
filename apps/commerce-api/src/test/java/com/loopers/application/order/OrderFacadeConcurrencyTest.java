package com.loopers.application.order;

import com.loopers.domain.coupon.Coupon;
import com.loopers.domain.coupon.CouponRepository;
import com.loopers.domain.coupon.CouponType;
import com.loopers.domain.coupon.Percent;
import com.loopers.domain.point.Point;
import com.loopers.domain.point.PointRepository;
import com.loopers.domain.product.Money;
import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.product.Stock;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserRepository;
import com.loopers.testhelper.ConcurrentTestResult;
import com.loopers.testhelper.ConcurrentTestRunner;
import com.loopers.common.constant.Gender;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class OrderFacadeConcurrencyTest {

    private final DatabaseCleanUp databaseCleanUp;
    private final OrderFacade orderFacade;

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final PointRepository pointRepository;
    private final CouponRepository couponRepository;

    @Autowired
    OrderFacadeConcurrencyTest(DatabaseCleanUp databaseCleanUp, OrderFacade orderFacade, UserRepository userRepository, ProductRepository productRepository, PointRepository pointRepository, CouponRepository couponRepository) {
        this.databaseCleanUp = databaseCleanUp;
        this.orderFacade = orderFacade;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.pointRepository = pointRepository;
        this.couponRepository = couponRepository;
    }

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @DisplayName("동시 주문 요청시")
    @Nested
    class PlaceOrderConcurrently {

        private User user1;
        private User user2;
        private Point point1;
        private Point point2;
        private Coupon coupon;
        private Product product1;
        private Product product2;
        private List<Product> savedProducts;

        @BeforeEach
        void setUp() {
            user1 = User.create("gukin1", "test1@email.com", "2025-10-10", Gender.FEMALE);
            user2 = User.create("gukin2", "test2@email.com", "2025-10-10", Gender.MALE);
            user1 = userRepository.save(user1);
            user2 = userRepository.save(user2);

            point1 = Point.create(user1, Money.of(1_000_000));
            point2 = Point.create(user2, Money.of(1_000_000));
            point1 = pointRepository.save(point1);
            point2 = pointRepository.save(point2);

            coupon = Coupon.create(CouponType.PERCENTAGE, user1.getId(), null, Percent.of(0.2));
            coupon = couponRepository.save(coupon);

            product1 = Product.create(Stock.of(1000), "맥북", Money.of(10_000), 1L);
            product2 = Product.create(Stock.of(1000), "아이폰", Money.of(5_000), 1L);
            savedProducts = productRepository.saveAll(List.of(product1, product2));
        }


        @DisplayName("동일한 쿠폰으로 여러 기기에서 동시에 주문해도, 쿠폰은 단 한번만 사용되어야 한다")
        @Test
        void useCouponExactlyOnce() throws Exception {
            // given
            List<OrderCommand.CartItem> items = savedProducts.stream()
                    .map(p -> new OrderCommand.CartItem(p.getId(), 1L))
                    .toList();

            OrderCommand.Create command = OrderCommand.Create.of(
                    user1.getId(),
                    items,
                    coupon.getId()
            );

            // when
            ConcurrentTestResult<OrderResult.Create> result = ConcurrentTestRunner.run(
                    10,
                    () -> orderFacade.create(command)
            );

            // then
            assertThat(result.getSuccesses()).hasSize(1);
            assertThat(result.getErrors()).hasSize(9);
        }

        @DisplayName("동일한 상품에 대해 여러 주문이 동시에 요청되어도, 재고가 정상적으로 차감되어야 한다")
        @Test
        void deductStocksProperly() throws Exception {
            // given
            List<OrderCommand.CartItem> items = savedProducts.stream()
                    .map(p -> new OrderCommand.CartItem(p.getId(), 1L))
                    .toList();

            OrderCommand.Create command = OrderCommand.Create.of(
                    user1.getId(),
                    items,
                    null
            );

            // when
            ConcurrentTestResult<OrderResult.Create> result = ConcurrentTestRunner.run(
                    10,
                    () -> orderFacade.create(command)
            );

            // then
            Product product1 = productRepository.findById(savedProducts.get(0).getId()).get();
            Product product2 = productRepository.findById(savedProducts.get(1).getId()).get();
            Assertions.assertAll(
                    () -> assertThat(result.getSuccesses()).hasSize(10),
                    () -> assertThat(result.getErrors()).hasSize(0),
                    () -> assertThat(product1.getStock().getQuantity()).isEqualTo(990),
                    () -> assertThat(product2.getStock().getQuantity()).isEqualTo(990)
            );
        }
    }
}
