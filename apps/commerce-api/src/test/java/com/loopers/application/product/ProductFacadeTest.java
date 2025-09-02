package com.loopers.application.product;

import com.loopers.application.common.dto.PagedResult;
import com.loopers.application.product.dto.*;
import com.loopers.domain.brand.Brand;
import com.loopers.domain.brand.BrandRepository;
import com.loopers.domain.product.*;
import com.loopers.utils.DatabaseCleanUp;
import com.loopers.utils.RedisCleanUp;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

@SpringBootTest
class ProductFacadeTest {

    @Autowired
    ProductRepository productRepository;

    @Autowired
    BrandRepository brandRepository;

    @Autowired
    ProductFacade productFacade;


    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    @Autowired
    private RedisCleanUp redisCleanUp;

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
        redisCleanUp.truncateAll();
    }

    @DisplayName("상품 상세 조회 시")
    @Nested
    class GetDetailView {

        @Test
        @DisplayName("상품 ID와 브랜드 ID로 상세 정보를 조회한다")
        void getProductDetail_success() {
            // Given
            Brand brand = Brand.create("Test Brand");
            Brand savedBrand = brandRepository.save(brand);

            Product product = Product.builder()
                    .name("Test Product")
                    .price(Money.of(1000L))
                    .stock(Stock.of(10L))
                    .status(ProductStatus.ACTIVE)
                    .brandId(savedBrand.getId())
                    .build();
            Product savedProduct = productRepository.save(product);

            ProductDetailQuery query = ProductDetailQuery.of(savedProduct.getId());

            // When
            ProductDetailView detailView = productFacade.getProductDetail(query);

            // Then
            Assertions.assertAll(
                    () -> assertNotNull(detailView),
                    () -> assertEquals(savedProduct.getId(), detailView.getProductId()),
                    () -> assertEquals(savedProduct.getName(), detailView.getProductName()),
                    () -> assertEquals(savedBrand.getName(), detailView.getBrandName())
            );
        }
    }

    @DisplayName("페이지별 상품 목록 조회 시")
    @Nested
    class GetPagedProducts {

        @Test
        @DisplayName("페이지네이션 정보와 함께 상품 목록을 조회한다")
        void getPagedProducts_success() {
            // Given
            Brand brand1 = brandRepository.save(Brand.create("Brand A"));
            Brand brand2 = brandRepository.save(Brand.create("Brand B"));

            // 상품 10개 생성 및 저장
            for (int i = 1; i <= 10; i++) {
                Brand currentBrand = (i % 2 == 0) ? brand2 : brand1;
                Product product = Product.builder()
                        .name("Product " + i)
                        .price(Money.of(100L * i))
                        .stock(Stock.of(10L))
                        .status(ProductStatus.ACTIVE)
                        .brandId(currentBrand.getId())
                        .likeCount(i)
                        .build();
                productRepository.save(product);
            }

            // 0페이지, 사이즈 5, 최신순 정렬 쿼리
            ProductPageQuery query = ProductPageQuery.create(0, 5, ProductSortType.LATEST);
            ProductPageQuery query2 = ProductPageQuery.create(1, 5, ProductSortType.LATEST);

            // When
            PagedResult<ProductSummaryView> result = productFacade.getPagedProducts(query);
            PagedResult<ProductSummaryView> result2 = productFacade.getPagedProducts(query2);

            // Then

            Assertions.assertAll(
                    // 0 페이지
                    () -> assertNotNull(result),
                    () -> assertEquals(5, result.getItems().size()), // 0페이지에 5개 아이템
                    () -> assertEquals(10, result.getTotalItems()), // 전체 아이템 10개
                    () -> assertEquals(0, result.getCurrentPage()), // 현재 페이지 0
                    () -> assertEquals(2, result.getTotalPages()), // 총 2페이지 (10개 / 5개)
                    () -> assertTrue(result.isHasNext()),// 다음 페이지 존재

                    // 정렬 및 내용 검증 (최신순이므로 Product 10, 9, 8, 7, 6 순서)
                    () -> assertEquals("Product 10", result.getItems().get(0).getProductName()),
                    () -> assertEquals("Brand B", result.getItems().get(0).getBrandName()),
                    () -> assertEquals("Product 6", result.getItems().get(4).getProductName()),
                    () -> assertEquals("Brand B", result.getItems().get(4).getBrandName()),

                    // 1페이지
                    () -> assertNotNull(result2),
                    () -> assertEquals(5, result2.getItems().size()),
                    () -> assertEquals(10, result2.getTotalItems()),
                    () -> assertEquals(1, result2.getCurrentPage()),
                    () -> assertEquals(2, result2.getTotalPages()),
                    () -> assertFalse(result2.isHasNext()), // 다음 페이지 없음

                    () -> assertEquals("Product 5", result2.getItems().get(0).getProductName()),
                    () -> assertEquals("Product 1", result2.getItems().get(4).getProductName())
            );

        }

        @Test
        @DisplayName("좋아요순으로 상품 목록을 조회한다")
        void getPagedProducts_sortByLikesDesc() {
            // Given
            Brand brand = brandRepository.save(Brand.create("Generic Brand"));

            // 좋아요 수가 다른 상품들 생성
            productRepository.save(Product.builder().name("P1").price(Money.of(100L)).stock(Stock.of(1L)).status(ProductStatus.ACTIVE).brandId(brand.getId()).likeCount(5).build());
            productRepository.save(Product.builder().name("P2").price(Money.of(200L)).stock(Stock.of(1L)).status(ProductStatus.ACTIVE).brandId(brand.getId()).likeCount(15).build());
            productRepository.save(Product.builder().name("P3").price(Money.of(300L)).stock(Stock.of(1L)).status(ProductStatus.ACTIVE).brandId(brand.getId()).likeCount(10).build());

            ProductPageQuery query = ProductPageQuery.create(0, 3, ProductSortType.LIKES_DESC);

            // When
            PagedResult<ProductSummaryView> result = productFacade.getPagedProducts(query);

            // Then
            assertNotNull(result);
            assertEquals(3, result.getItems().size());
            assertEquals(3, result.getTotalItems());
            assertEquals(0, result.getCurrentPage());
            assertEquals(1, result.getTotalPages());
            assertFalse(result.isHasNext());

            // 좋아요순 내림차순 검증 (15, 10, 5)
            assertEquals("P2", result.getItems().get(0).getProductName());
            assertEquals("P3", result.getItems().get(1).getProductName());
            assertEquals("P1", result.getItems().get(2).getProductName());
        }

        @Test
        @DisplayName("가격 낮은순으로 상품 목록을 조회한다")
        void getPagedProducts_sortByPriceAsc() {
            // Given
            Brand brand = brandRepository.save(Brand.create("Generic Brand"));

            // 가격이 다른 상품들 생성
            productRepository.save(Product.builder().name("P1").price(Money.of(300L)).stock(Stock.of(1L)).status(ProductStatus.ACTIVE).brandId(brand.getId()).likeCount(1).build());
            productRepository.save(Product.builder().name("P2").price(Money.of(100L)).stock(Stock.of(1L)).status(ProductStatus.ACTIVE).brandId(brand.getId()).likeCount(1).build());
            productRepository.save(Product.builder().name("P3").price(Money.of(200L)).stock(Stock.of(1L)).status(ProductStatus.ACTIVE).brandId(brand.getId()).likeCount(1).build());

            ProductPageQuery query = ProductPageQuery.create(0, 3, ProductSortType.PRICE_ASC);

            // When
            PagedResult<ProductSummaryView> result = productFacade.getPagedProducts(query);

            // Then
            Assertions.assertAll(
                    () -> assertNotNull(result),
                    () -> assertEquals(3, result.getItems().size()),
                    () -> assertEquals(3, result.getTotalItems()),
                    () -> assertEquals(0, result.getCurrentPage()),
                    () -> assertEquals(1, result.getTotalPages()),
                    () -> assertFalse(result.isHasNext()),

                    // 가격 낮은순 오름차순 검증 (100, 200, 300)
                    () -> assertEquals("P2", result.getItems().get(0).getProductName()),
                    () -> assertEquals("P3", result.getItems().get(1).getProductName()),
                    () -> assertEquals("P1", result.getItems().get(2).getProductName())
            );

        }

        @Test
        @DisplayName("상품이 없을 경우 빈 목록과 올바른 페이지 정보를 반환한다")
        void getPagedProducts_noProducts() {
            // Given
            ProductPageQuery query = ProductPageQuery.create(0, 10, ProductSortType.LATEST);

            // When
            PagedResult<ProductSummaryView> result = productFacade.getPagedProducts(query);

            // Then
            assertNotNull(result);
            assertTrue(result.getItems().isEmpty());
            assertEquals(0, result.getTotalItems());
            assertEquals(0, result.getCurrentPage());
            assertEquals(0, result.getTotalPages());
            assertFalse(result.isHasNext());
        }
    }
}
