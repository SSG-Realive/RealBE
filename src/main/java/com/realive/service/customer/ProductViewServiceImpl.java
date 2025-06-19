package com.realive.service.customer;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.realive.domain.common.enums.MediaType;
import com.realive.domain.product.Product;
import com.realive.dto.page.PageRequestDTO;
import com.realive.dto.page.PageResponseDTO;
import com.realive.dto.product.ProductListDTO;
import com.realive.dto.product.ProductResponseDTO;
import com.realive.repository.customer.productview.ProductDetail;
import com.realive.repository.customer.productview.ProductSearch;
import com.realive.repository.product.ProductImageRepository;
import com.realive.repository.product.ProductRepository;

import jakarta.persistence.EntityNotFoundException;
import lombok.extern.log4j.Log4j2;

// [Customer] 상품 조회 Service 구현체

@Service
@Transactional
@Log4j2
public class ProductViewServiceImpl implements ProductViewService {

    private final ProductSearch productSearch;
    private final ProductDetail productDetail;
    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository; // ✅ 추가

    public ProductViewServiceImpl(
            @Qualifier("productSearchImpl") ProductSearch productSearch,
            @Qualifier("productDetailImpl") ProductDetail productDetail,
            ProductRepository productRepository,
            ProductImageRepository productImageRepository // ✅ 생성자 주입
    ) {
        this.productSearch = productSearch;
        this.productDetail = productDetail;
        this.productRepository = productRepository;
        this.productImageRepository = productImageRepository;
    }

    @Override
    public PageResponseDTO<ProductListDTO> search(PageRequestDTO dto, Long categoryId) {
        return productSearch.search(dto, categoryId);
    }

    @Override
    public ProductResponseDTO getProductDetail(Long id) {
        return productDetail.findProductDetailById(id)
                .orElseThrow(() -> new EntityNotFoundException("해당 상품이 존재하지 않습니다. id=" + id));
    }

    // ✅ 관련 상품 추천
    public List<ProductListDTO> getRelatedProducts(Long productId) {
        // 1. 기준 상품 확인
        Product target = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("상품이 존재하지 않습니다. id=" + productId));

        // 2. 같은 카테고리의 다른 상품 6개 조회
        List<Product> relatedProducts = productRepository
                .findTop6ByCategoryIdAndIdNotAndActiveTrue(
                        target.getCategory().getId(),
                        target.getId()
                );

        // 3. 상품 ID 리스트 추출
        List<Long> productIds = relatedProducts.stream()
                .map(Product::getId)
                .toList();

        // 4. 대표 이미지 URL 조회
        List<Object[]> rows = productImageRepository.findThumbnailUrlsByProductIds(productIds, MediaType.IMAGE);
        Map<Long, String> imageMap = rows.stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],
                        row -> (String) row[1]
                ));

        // 5. DTO 변환
        return relatedProducts.stream()
                .map(product -> ProductListDTO.from(product, imageMap.get(product.getId())))
                .toList();
    }
}
