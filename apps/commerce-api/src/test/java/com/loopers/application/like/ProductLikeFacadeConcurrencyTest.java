package com.loopers.application.like;

import com.loopers.domain.product.Product;
import com.loopers.domain.product.ProductRepository;
import com.loopers.domain.user.User;
import com.loopers.domain.user.UserRepository;
import com.loopers.testhelper.ConcurrentTestRunner;
import com.loopers.common.constant.Gender;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest
class ProductLikeFacadeConcurrencyTest {


    @Autowired
    ProductRepository productRepository;
    @Autowired
    UserRepository userRepository;

    @Autowired
    ProductLikeFacade sut;

    @Autowired
    DatabaseCleanUp databaseCleanUp;
    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @DisplayName("한 명의 유저가 상품 좋아요를 동시에 여러 번 눌러도 좋아요 수는 1만 오릅니다.")
    @Test
    void like_idempotency_under_concurrency() throws Exception {
        // given
        User user = userRepository.save(User.create("gukin", "gukin@email.com", "2023-10-01", Gender.FEMALE));
        Product product = productRepository.save(new Product(10));
        ProductLikeCommand.Like command = ProductLikeCommand.Like.of(user.getLoginId(), product.getId());

        // when
        ConcurrentTestRunner.run(5, () -> {
            sut.like(command);
            return null;
        });

        // then
        await().during(300, TimeUnit.MILLISECONDS).atMost(3, TimeUnit.SECONDS).untilAsserted(() -> {
            Product result = productRepository.findById(product.getId()).get();
            assertThat(result.getLikeCount()).isEqualTo(1);
        });
    }

    @DisplayName("한 명의 유저가 상품 좋아요 취소를 동시에 여러 번 눌러도 좋아요 수는 1만 감소합니다.")
    @Test
    void unlike_idempotency_under_concurrency() throws Exception {
        // given
        User user = userRepository.save(User.create("gukin", "gukin@email.com", "2023-10-01", Gender.FEMALE));
        Product product = productRepository.save(new Product(10));
        ProductLikeCommand.Like productLikeCommand = ProductLikeCommand.Like.of(user.getLoginId(), product.getId());
        sut.like(productLikeCommand);

        ProductLikeCommand.Unlike unlikeCommand = ProductLikeCommand.Unlike.of(user.getLoginId(), product.getId());

        // when
        ConcurrentTestRunner.run(5, () -> {
            sut.unlike(unlikeCommand);
            return null;
        });

        // then
        Product result = productRepository.findById(product.getId()).get();
        Assertions.assertAll(
                () -> assertThat(result.getLikeCount()).isZero()
        );
    }

    @DisplayName("여러 사용자가 동시에 상품 좋아요를 누르면, 좋아요 수가 사용자 수만큼 증가한다.")
    @Test
    void like_concurrently_by_multiple_users() throws Exception {
        // given
        int threadCount = 5;
        Product product = productRepository.save(new Product(100));

        // 1. 여러 사용자 생성 및 저장
        List<User> users = IntStream.range(0, threadCount)
                .mapToObj(i -> User.create("user" + i, "user" + i + "@email.com", "2025-01-01", Gender.MALE))
                .collect(Collectors.toList());
        userRepository.saveAll(users);

        // 2. 각 사용자에 대한 '좋아요' 커맨드를 스레드-안전 큐에 추가
        ConcurrentLinkedQueue<ProductLikeCommand.Like> commands = new ConcurrentLinkedQueue<>();
        users.forEach(user -> commands.add(ProductLikeCommand.Like.of(user.getLoginId(), product.getId())));

        // when
        ConcurrentTestRunner.run(threadCount, () -> {
            ProductLikeCommand.Like command = commands.poll();
            if (command != null) {
                sut.like(command);
            }
            return null;
        });

        // then
        await().during(300, TimeUnit.MILLISECONDS).atMost(3, TimeUnit.SECONDS).untilAsserted(() -> {
            Product result = productRepository.findById(product.getId()).get();
            assertThat(result.getLikeCount()).isEqualTo(users.size());
        });
    }

    @DisplayName("여러 사용자가 동시에 상품 좋아요를 취소하면, 좋아요 수가 0이 된다.")
    @Test
    void unlike_concurrently_by_multiple_users() throws Exception {
        // given
        int threadCount = 5;
        Product product = productRepository.save(new Product(100));

        // 1. 여러 사용자 생성 및 저장
        List<User> users = IntStream.range(0, threadCount)
                .mapToObj(i -> User.create("user" + i, "user" + i + "@email.com", "2025-01-01", Gender.MALE))
                .collect(Collectors.toList());
        userRepository.saveAll(users);

        // 2. 모든 사용자가 먼저 '좋아요'를 누른 상태로 만듦
        users.forEach(user -> {
            ProductLikeCommand.Like productLikeCommand = ProductLikeCommand.Like.of(user.getLoginId(), product.getId());
            sut.like(productLikeCommand);
        });
        Product initialProduct = productRepository.findById(product.getId()).get();
        assertThat(initialProduct.getLikeCount()).isEqualTo(threadCount); // 초기 상태 검증

        // 3. 각 사용자에 대한 '좋아요 취소' 커맨드를 큐에 추가
        ConcurrentLinkedQueue<ProductLikeCommand.Unlike> commands = new ConcurrentLinkedQueue<>();
        users.forEach(user -> commands.add(ProductLikeCommand.Unlike.of(user.getLoginId(), product.getId())));

        // when
        ConcurrentTestRunner.run(threadCount, () -> {
            ProductLikeCommand.Unlike command = commands.poll();
            if (command != null) {
                sut.unlike(command);
            }
            return null;
        });

        // then
        await().during(300, TimeUnit.MILLISECONDS).atMost(3, TimeUnit.SECONDS).untilAsserted(() -> {
            Product result = productRepository.findById(product.getId()).get();
            assertThat(result.getLikeCount()).isZero();
        });
    }
}
