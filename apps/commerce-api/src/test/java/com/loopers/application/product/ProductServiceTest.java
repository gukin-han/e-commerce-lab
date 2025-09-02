package com.loopers.application.product;

import com.loopers.domain.product.ProductService;
import com.loopers.common.error.CoreException;
import com.loopers.utils.DatabaseCleanUp;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ProductServiceTest {

    @Autowired
    ProductService productService;

    @Autowired
    DatabaseCleanUp databaseCleanUp;
    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @DisplayName("상품 조회시")
    @Nested
    class findByProductId {

        @DisplayName("존재하지 않는 상품 ID로 상품 조회하는 경우 CoreException 예외를 던진다")
        @Test
        void throwsEntityNotFoundException_whenProductIdIsNotFound() {
            Assertions.assertThatThrownBy(() -> productService.findByProductId(999L))
                    .isInstanceOf(CoreException.class);
        }
    }
}
