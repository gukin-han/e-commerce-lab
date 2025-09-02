package com.loopers.interfaces.api.product;

import com.loopers.domain.brand.Brand;
import com.loopers.domain.brand.BrandRepository;
import com.loopers.domain.product.*;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.utils.DatabaseCleanUp;
import com.loopers.utils.RedisCleanUp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ProductV1ControllerTest {

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private BrandRepository brandRepository;

    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    @Autowired
    private RedisCleanUp redisCleanUp;

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
        redisCleanUp.truncateAll();
    }

    @DisplayName("GET /api/v1/products")
    @Nested
    class GetProducts {

        @Test
        @DisplayName("상품 목록 조회가 성공할 경우, 페이징 처리된 상품 목록을 반환한다.")
        void returnsPagedProducts_whenProductsExist() {
            // given
            Brand brand = brandRepository.save(Brand.create("Test Brand"));
            for (int i = 0; i < 20; i++) {
                productRepository.save(Product.builder()
                        .name("Product " + i)
                        .price(Money.of(1000L * (i + 1)))
                        .stock(Stock.of(10L))
                        .status(ProductStatus.ACTIVE)
                        .brandId(brand.getId())
                        .likeCount(i)
                        .build());
            }

            URI uri = UriComponentsBuilder.fromUriString("/api/v1/products")
                    .queryParam("page", "0")
                    .queryParam("size", "5")
                    .queryParam("sortType", "LATEST")
                    .build()
                    .toUri();

            // when
            ResponseEntity<ApiResponse<ProductV1Dto.GetProductsResponse>> response = testRestTemplate.exchange(
                    uri,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<>() {}
            );

            // then
            assertAll(
                    () -> assertTrue(response.getStatusCode().is2xxSuccessful()),
                    () -> assertThat(Objects.requireNonNull(response.getBody()).data()).isNotNull(),
                    () -> assertThat(Objects.requireNonNull(response.getBody()).data().getItems()).hasSize(5),
                    () -> assertThat(Objects.requireNonNull(response.getBody()).data().getCurrentPage()).isEqualTo(0),
                    () -> assertThat(Objects.requireNonNull(response.getBody()).data().getTotalPages()).isEqualTo(4),
                    () -> assertThat(Objects.requireNonNull(response.getBody()).data().getTotalItems()).isEqualTo(20),
                    () -> assertThat(Objects.requireNonNull(response.getBody()).data().isHasNext()).isTrue(),
                    () -> assertThat(Objects.requireNonNull(response.getBody()).data().getItems().get(0).getProductName()).isEqualTo("Product 19")
            );
        }

        @Test
        @DisplayName("brandId로 필터링하여 상품 목록 조회가 성공할 경우, 해당 브랜드의 상품 목록을 반환한다.")
        void returnsFilteredProducts_whenBrandIdIsProvided() {
            // given
            Brand brandA = brandRepository.save(Brand.create("Brand A"));
            Brand brandB = brandRepository.save(Brand.create("Brand B"));

            for (int i = 0; i < 5; i++) {
                productRepository.save(Product.builder()
                        .name("Product A " + i)
                        .price(Money.of(1000L * (i + 1)))
                        .stock(Stock.of(10L))
                        .status(ProductStatus.ACTIVE)
                        .brandId(brandA.getId())
                        .likeCount(i)
                        .build());
            }
            for (int i = 0; i < 3; i++) {
                productRepository.save(Product.builder()
                        .name("Product B " + i)
                        .price(Money.of(1000L * (i + 1)))
                        .stock(Stock.of(10L))
                        .status(ProductStatus.ACTIVE)
                        .brandId(brandB.getId())
                        .likeCount(i)
                        .build());
            }

            URI uri = UriComponentsBuilder.fromUriString("/api/v1/products")
                    .queryParam("page", "0")
                    .queryParam("size", "10")
                    .queryParam("sortType", "LATEST")
                    .queryParam("brandId", brandA.getId())
                    .build()
                    .toUri();

            // when
            ResponseEntity<ApiResponse<ProductV1Dto.GetProductsResponse>> response = testRestTemplate.exchange(
                    uri,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<>() {}
            );

            // then
            assertAll(
                    () -> assertTrue(response.getStatusCode().is2xxSuccessful()),
                    () -> assertThat(Objects.requireNonNull(response.getBody()).data()).isNotNull(),
                    () -> assertThat(Objects.requireNonNull(response.getBody()).data().getItems()).hasSize(5),
                    () -> assertThat(Objects.requireNonNull(response.getBody()).data().getTotalItems()).isEqualTo(5),
                    () -> assertThat(Objects.requireNonNull(response.getBody()).data().getItems())
                            .allMatch(p -> p.getBrandName().equals("Brand A"))
            );
        }
    }

    @DisplayName("GET /api/v1/products/{productId}")
    @Nested
    class GetProductById {

        @Test
        @DisplayName("상품 상세 조회가 성공할 경우, 상품 상세 정보를 반환한다.")
        void returnsProductDetails_whenProductExists() {
            // given
            Brand brand = brandRepository.save(Brand.create("Test Brand"));
            Product product = productRepository.save(Product.builder()
                    .name("Test Product")
                    .price(Money.of(1000L))
                    .stock(Stock.of(10L))
                    .status(ProductStatus.ACTIVE)
                    .brandId(brand.getId())
                    .build());

            // when
            ResponseEntity<ApiResponse<ProductV1Dto.GetProductByIdResponse>> response = testRestTemplate.exchange(
                    "/api/v1/products/{productId}",
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<>() {},
                    product.getId()
            );

            // then
            assertAll(
                    () -> assertTrue(response.getStatusCode().is2xxSuccessful()),
                    () -> assertThat(Objects.requireNonNull(response.getBody()).data()).isNotNull(),
                    () -> assertThat(Objects.requireNonNull(response.getBody()).data().getProductId()).isEqualTo(product.getId()),
                    () -> assertThat(Objects.requireNonNull(response.getBody()).data().getProductName()).isEqualTo("Test Product"),
                    () -> assertThat(Objects.requireNonNull(response.getBody()).data().getBrandName()).isEqualTo("Test Brand")
            );
        }

        @Test
        @DisplayName("존재하지 않는 상품 ID로 조회할 경우, 404 Not Found 응답을 반환한다.")
        void returns404NotFound_whenProductDoesNotExist() {
            // when
            ResponseEntity<ApiResponse<ProductV1Dto.GetProductByIdResponse>> response = testRestTemplate.exchange(
                    "/api/v1/products/{productId}",
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<>() {},
                    999L
            );

            // then
            assertAll(
                    () -> assertTrue(response.getStatusCode().is4xxClientError()),
                    () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND)
            );
        }
    }
}
