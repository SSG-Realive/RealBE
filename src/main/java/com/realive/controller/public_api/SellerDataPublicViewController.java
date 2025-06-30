// src/main/java/com/realive/controller.public_api/SellerInfoDetailViewController.java
package com.realive.controller.public_api;

import com.realive.dto.page.PageRequestDTO; // PageRequestDTO import
import com.realive.dto.page.PageResponseDTO; // PageResponseDTO import
import com.realive.dto.product.ProductListDTO; // ProductListDTO import
import com.realive.dto.product.ProductSearchCondition; // ProductSearchCondition import
import com.realive.dto.review.ReviewResponseDTO; // ReviewResponseDTO import
import com.realive.dto.seller.SellerPublicResponseDTO; // SellerPublicResponseDTO import
import com.realive.service.product.ProductService; // ProductService import
import com.realive.service.review.view.ReviewViewService;   // ReviewService import
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2; // Log4j2 사용
import org.springframework.data.domain.Pageable; // Spring Data의 Pageable import
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/api/public/seller") // 기본 경로: /api/public/seller
@Log4j2 // 로깅 활성화
@RequiredArgsConstructor // final 필드들을 자동으로 주입 (생성자 주입)
public class SellerDataPublicViewController {

    private final ProductService productService; // ProductService 주입
    private final ReviewViewService reviewService;   // ReviewService 주입

    /**
     * 특정 상품 ID를 통해 판매자의 공개 정보를 조회합니다.
     * 엔드포인트: GET /api/public/seller/by-product/{productId}
     *
     * @param productId 상품 ID
     * @return 판매자의 공개 정보 DTO (SellerPublicResponseDTO) 또는 404 Not Found 응답
     */
    @GetMapping("/by-product/{productId}")
    public ResponseEntity<SellerPublicResponseDTO> getPublicSellerInfoByProductId(@PathVariable Long productId) {
        log.info("상품 ID {} 로 판매자 공개 정보 조회를 요청받았습니다.", productId);
        Optional<SellerPublicResponseDTO> sellerPublicInfo = productService.getPublicSellerInfoByProductId(productId);

        // Optional이 비어있으면 404 Not Found 응답, 아니면 200 OK와 함께 DTO 반환
        return sellerPublicInfo.map(ResponseEntity::ok)
                .orElseGet(() -> {
                    log.warn("상품 ID {} 에 해당하는 판매자 공개 정보를 찾을 수 없습니다.", productId);
                    return ResponseEntity.notFound().build();
                });
    }

    /**
     * 특정 판매자 ID에 대한 리뷰 목록을 페이지네이션하여 조회합니다.
     * 엔드포인트: GET /api/public/seller/{sellerId}/reviews?page={page}&size={size}&sort={sort}&direction={direction}
     *
     * @param sellerId 판매자 ID
     * @param pageRequestDTO 페이지 요청 정보 (PageRequestDTO 사용)
     * @return 리뷰 목록 및 페이지 정보가 담긴 DTO (PageResponseDTO<ReviewResponseDTO>)
     */
    @GetMapping("/{sellerId}/reviews")
    public ResponseEntity<PageResponseDTO<ReviewResponseDTO>> getSellerReviews(
            @PathVariable Long sellerId,
            PageRequestDTO pageRequestDTO) { // PageRequestDTO를 직접 인자로 받음

        log.info("판매자 ID {} 에 대한 리뷰 목록 조회를 요청받았습니다. 요청 페이지 정보: {}", sellerId, pageRequestDTO);

        // PageRequestDTO의 toPageable() 메서드를 사용하여 Pageable 객체 생성
        Pageable pageable = pageRequestDTO.toPageable();

        PageResponseDTO<ReviewResponseDTO> reviews = reviewService.getSellerReviews(sellerId, pageable);
        return ResponseEntity.ok(reviews);
    }

    /**
     * 특정 판매자 ID에 대한 상품 목록을 페이지네이션하여 조회합니다.
     * 엔드포인트: GET /api/public/seller/{sellerId}/products?page={page}&size={size}&orderBy={orderBy}&order={order}
     * (프론트엔드에서는 orderBy=createdAt&order=desc 형태의 쿼리 파라미터를 사용하므로 ProductSearchCondition에 매핑)
     *
     * @param sellerId 판매자 ID
     * @param productSearchCondition 상품 검색 조건 (페이지, 사이즈, 정렬 필드/방향 포함)
     * @return 상품 목록 및 페이지 정보가 담긴 DTO (PageResponseDTO<ProductListDTO>)
     */
    @GetMapping("/{sellerId}/products")
    public ResponseEntity<PageResponseDTO<ProductListDTO>> getSellerProducts(
            @PathVariable Long sellerId,
            ProductSearchCondition productSearchCondition) { // ProductSearchCondition을 직접 인자로 받음

        log.info("판매자 ID {} 에 대한 상품 목록 조회를 요청받았습니다. 검색 조건: {}", sellerId, productSearchCondition);

        // ProductSearchCondition DTO에 sellerId를 설정할 필요는 없습니다.
        // ProductService의 getProductsBySeller 메서드는 sellerId와 condition을 별도로 받으므로
        // Service 단에서 sellerId를 활용합니다.

        PageResponseDTO<ProductListDTO> products = productService.getProductsBySeller(sellerId, productSearchCondition);
        return ResponseEntity.ok(products);
    }
}