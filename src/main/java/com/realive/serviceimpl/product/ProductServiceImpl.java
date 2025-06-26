package com.realive.serviceimpl.product;

import com.realive.domain.common.enums.MediaType;
import com.realive.domain.product.*;
import com.realive.domain.seller.Seller;
import com.realive.dto.admin.review.SellerRankingDTO;
import com.realive.dto.page.PageResponseDTO;
import com.realive.dto.product.*;
import com.realive.repository.product.*;
import com.realive.repository.seller.SellerRepository;
import com.realive.service.admin.logs.StatService;
import com.realive.service.common.FileUploadService;
import com.realive.service.product.ProductService;
import com.realive.service.seller.SellerService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductServiceImpl implements ProductService {

        private static final Logger log = LoggerFactory.getLogger(ProductServiceImpl.class);

        private final ProductImageRepository productImageRepository;
        private final ProductRepository productRepository;
        private final CategoryRepository categoryRepository;
        private final SellerRepository sellerRepository;
        private final DeliveryPolicyRepository deliveryPolicyRepository;
        private final FileUploadService fileUploadService;
        private final SellerService sellerService;
        private final StatService statService;

        @Override
        public Long createProduct(ProductRequestDTO dto, Long sellerId) {
                Seller seller = sellerRepository.findById(sellerId)
                                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 판매자입니다."));

                Category category = null;
                if (dto.getCategoryId() != null) {
                        category = categoryRepository.findById(dto.getCategoryId())
                                        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 카테고리입니다."));
                }

                Product product = Product.builder()
                                .name(dto.getName())
                                .description(dto.getDescription())
                                .price(dto.getPrice())
                                .stock(dto.getStock() != null ? dto.getStock() : 1)
                                .width(dto.getWidth())
                                .depth(dto.getDepth())
                                .height(dto.getHeight())
                                .status(dto.getStatus())
                                .active(dto.getActive() != null ? dto.getActive() : true)
                                .category(category)
                                .seller(seller)
                                .build();

                productRepository.save(product);

                // 대표 이미지 저장
                String imageUrl = fileUploadService.upload(dto.getImageThumbnail(), "product", sellerId);
                productImageRepository.save(ProductImage.builder()
                                .url(imageUrl)
                                .isThumbnail(true)
                                .mediaType(MediaType.IMAGE)
                                .product(product)
                                .build());

                // 대표 영상 저장 (선택)
                if (dto.getVideoThumbnail() != null && !dto.getVideoThumbnail().isEmpty()) {
                        String videoUrl = fileUploadService.upload(dto.getVideoThumbnail(), "product", sellerId);
                        productImageRepository.save(ProductImage.builder()
                                        .url(videoUrl)
                                        .isThumbnail(true)
                                        .mediaType(MediaType.VIDEO)
                                        .product(product)
                                        .build());
                }

                // 서브 이미지 저장
                if (dto.getSubImages() != null && !dto.getSubImages().isEmpty()) {
                        for (MultipartFile file : dto.getSubImages()) {
                                if (file != null && !file.isEmpty()) {
                                        String url = fileUploadService.upload(file, "product", sellerId);
                                        productImageRepository.save(ProductImage.builder()
                                                        .url(url)
                                                        .isThumbnail(false)
                                                        .mediaType(MediaType.IMAGE)
                                                        .product(product)
                                                        .build());
                                }
                        }
                }

                // 배송 정책 저장
                if (dto.getDeliveryPolicy() != null) {
                        DeliveryPolicy policy = DeliveryPolicy.builder()
                                        .type(dto.getDeliveryPolicy().getType())
                                        .cost(dto.getDeliveryPolicy().getCost())
                                        .regionLimit(dto.getDeliveryPolicy().getRegionLimit())
                                        .product(product)
                                        .build();
                        deliveryPolicyRepository.save(policy);
                }

                return product.getId();
        }

        @Override
        public void updateProduct(Long productId, ProductRequestDTO dto, Long sellerId) {
                Product product = productRepository.findById(productId)
                                .orElseThrow(() -> new IllegalArgumentException("상품이 존재하지 않습니다."));

                if (!product.getSeller().getId().equals(sellerId)) {
                        throw new SecurityException("해당 상품에 대한 수정 권한이 없습니다.");
                }

                // 대표 이미지 저장
                if (dto.getImageThumbnail() != null && !dto.getImageThumbnail().isEmpty()) {
                        productImageRepository.findByProductId(productId).stream()
                                        .filter(img -> img.isThumbnail() && img.getMediaType() == MediaType.IMAGE)
                                        .forEach(productImageRepository::delete);

                        String imageUrl = fileUploadService.upload(dto.getImageThumbnail(), "product", sellerId);
                        productImageRepository.save(ProductImage.builder()
                                        .url(imageUrl)
                                        .isThumbnail(true)
                                        .mediaType(MediaType.IMAGE)
                                        .product(product)
                                        .build());
                }

                // 대표 영상 저장
                if (dto.getVideoThumbnail() != null && !dto.getVideoThumbnail().isEmpty()) {
                        productImageRepository.findByProductId(productId).stream()
                                        .filter(img -> img.isThumbnail() && img.getMediaType() == MediaType.VIDEO)
                                        .forEach(productImageRepository::delete);

                        String videoUrl = fileUploadService.upload(dto.getVideoThumbnail(), "product", sellerId);
                        productImageRepository.save(ProductImage.builder()
                                        .url(videoUrl)
                                        .isThumbnail(true)
                                        .mediaType(MediaType.VIDEO)
                                        .product(product)
                                        .build());
                }

                // 서브 이미지 저장
                if (dto.getSubImages() != null && !dto.getSubImages().isEmpty()) {
                        for (MultipartFile file : dto.getSubImages()) {
                                if (file != null && !file.isEmpty()) {
                                        String url = fileUploadService.upload(file, "product", sellerId);
                                        productImageRepository.save(ProductImage.builder()
                                                        .url(url)
                                                        .isThumbnail(false)
                                                        .mediaType(MediaType.IMAGE)
                                                        .product(product)
                                                        .build());
                                }
                        }
                }

                // 상품 정보 수정
                product.setName(dto.getName());
                product.setDescription(dto.getDescription());
                product.setPrice(dto.getPrice());
                product.setStock(dto.getStock() != null ? dto.getStock() : product.getStock());
                product.setWidth(dto.getWidth());
                product.setDepth(dto.getDepth());
                product.setHeight(dto.getHeight());
                product.setStatus(dto.getStatus());

                // 🚩 isActive 처리 로직 수정
                if (dto.getActive() != null && dto.getActive()) {
                        // 활성화 요청 시 → stock 검사
                        if (product.getStock() >= 1) {
                                product.setActive(true);
                        } else {
                                product.setActive(false); // 또는 예외 발생 가능
                                log.warn("재고가 없는 상품은 활성화할 수 없습니다. productId={}", product.getId());
                        }
                } else if (dto.getActive() != null && !dto.getActive()) {
                        // 비활성화 요청은 그대로 반영
                        product.setActive(false);
                }

                if (dto.getCategoryId() != null) {
                        Category category = categoryRepository.findById(dto.getCategoryId())
                                        .orElseThrow(() -> new IllegalArgumentException("카테고리를 찾을 수 없습니다."));
                        product.setCategory(category);
                }

                if (dto.getDeliveryPolicy() != null) {
                        DeliveryPolicy policy = deliveryPolicyRepository.findByProduct(product)
                                        .orElse(new DeliveryPolicy());

                        policy.setProduct(product);
                        policy.setType(dto.getDeliveryPolicy().getType());
                        policy.setCost(dto.getDeliveryPolicy().getCost());
                        policy.setRegionLimit(dto.getDeliveryPolicy().getRegionLimit());

                        deliveryPolicyRepository.save(policy);
                }

                productRepository.save(product);
        }

        @Override
        public void deleteProduct(Long productId, Long sellerId) {
                Product product = productRepository.findById(productId)
                                .orElseThrow(() -> new IllegalArgumentException("상품이 존재하지 않습니다."));

                if (!product.getSeller().getId().equals(sellerId)) {
                        throw new SecurityException("해당 상품에 대한 삭제 권한이 없습니다.");
                }

                product.setActive(false);
        }

        // 상품 목록 조회 (판매자 전용)
        @Override
        public PageResponseDTO<ProductListDTO> getProductsBySeller(Long sellerId, ProductSearchCondition condition) {

                Page<Product> result = productRepository.searchProducts(condition, sellerId);
                List<Product> products = result.getContent();

                List<Long> productIds = products.stream()
                                .map(Product::getId)
                                .toList();

                List<Object[]> rows = productImageRepository.findThumbnailUrlsByProductIds(productIds, MediaType.IMAGE);
                Map<Long, String> imageMap = rows.stream()
                                .collect(Collectors.toMap(
                                                row -> (Long) row[0],
                                                row -> (String) row[1]));

                List<ProductListDTO> dtoList = products.stream()
                                .map(product -> ProductListDTO.from(product, imageMap.get(product.getId())))
                                .collect(Collectors.toList());

                return PageResponseDTO.<ProductListDTO>withAll()
                                .pageRequestDTO(condition)
                                .dtoList(dtoList)
                                .total((int) result.getTotalElements())
                                .build();
        }

        @Override
        public ProductResponseDTO getProductDetail(Long productId, Long sellerId) {

                Product product = productRepository.findById(productId)
                                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));

                Category category = product.getCategory();
                if (!product.getSeller().getId().equals(sellerId)) {
                        throw new SecurityException("해당 상품에 대한 조회 권한이 없습니다.");
                }
                return ProductResponseDTO.builder()
                                .id(product.getId())
                                .name(product.getName())
                                .description(product.getDescription())
                                .price(product.getPrice())
                                .stock(product.getStock())
                                .width(product.getWidth())
                                .depth(product.getDepth())
                                .height(product.getHeight())
                                .status(product.getStatus().name())
                                .isActive(product.isActive())
                                .imageThumbnailUrl(getThumbnailUrlByType(productId, MediaType.IMAGE))
                                .videoThumbnailUrl(getThumbnailUrlByType(productId, MediaType.VIDEO))
                                .categoryName(Category.getCategoryFullPath(product.getCategory()))
                                .categoryId(category.getId())
                                .parentCategoryId(category.getParent() != null ? category.getParent().getId() : null) // ✅
                                                                                                                      // 추가
                                .sellerName(product.getSeller().getName())
                                .sellerId(product.getSeller().getId())
                                .build();
        }

        public String getThumbnailUrlByType(Long productId, MediaType mediaType) {
                return productImageRepository
                                .findFirstByProductIdAndIsThumbnailTrueAndMediaType(productId, mediaType)
                                .map(ProductImage::getUrl)
                                .orElse(null);
        }

        @Override
        public PageResponseDTO<ProductListDTO> getAllProductsForAdmin(ProductSearchCondition condition) {
                log.info("관리자용 전체 상품 목록 조회 - 조건: {}", condition);

                // QueryDSL 기반 커스텀 메서드 사용
                Page<Product> result = productRepository.searchProducts(condition, null); // sellerId 없이 전체 조회
                List<Product> products = result.getContent();

                // 상품 id 목록 추출
                List<Long> productIds = products.stream()
                                .map(Product::getId)
                                .toList();

                // 썸네일 이미지 매핑
                List<Object[]> rows = productImageRepository.findThumbnailUrlsByProductIds(productIds, MediaType.IMAGE);
                Map<Long, String> imageMap = rows.stream()
                                .collect(Collectors.toMap(
                                                row -> (Long) row[0],
                                                row -> (String) row[1]));

                // DTO 변환
                List<ProductListDTO> dtoList = products.stream()
                                .map(product -> ProductListDTO.from(product, imageMap.get(product.getId())))
                                .collect(Collectors.toList());

                return PageResponseDTO.<ProductListDTO>withAll()
                                .pageRequestDTO(condition)
                                .dtoList(dtoList)
                                .total((int) result.getTotalElements())
                                .build();
        }

        /**
         * 추천용: 후보 상위 candidateSize명 중 sellersPick명 랜덤 추출,
         * 각 셀러당 productsPerSeller개 랜덤 상품을 뽑아 DTO로 반환
         */
        @Override
        public List<FeaturedSellerProductsResponseDTO> getFeaturedSellersWithProducts(
                        int candidateSize,
                        int sellersPick,
                        int productsPerSeller,
                        long minReviews) {
                // 1) 상위 후보 셀러 조회
                List<SellerRankingDTO> originalRankings  = statService
                                .getRanking(minReviews, PageRequest.of(0, candidateSize))
                                .getContent();

                // 2) 랜덤 셔플 및 sellersPick 수만큼 선택
                List<SellerRankingDTO> rankings = new ArrayList<>(originalRankings);
                Collections.shuffle(rankings);
                List<SellerRankingDTO> picked = rankings.subList(0, Math.min(sellersPick, rankings.size()));

                // 3) 셀러별 랜덤 상품 + 썸네일 URL 매핑 → DTO
                return picked.stream()
                                .map(seller -> {
                                        // (a) 랜덤 상품 조회
                                        List<Product> prods = productRepository
                                                        .findRandomProductsBySellerId(seller.getSellerId(),
                                                                        productsPerSeller);

                                        // (b) 상품 ID 목록 생성
                                        List<Long> prodIds = prods.stream()
                                                        .map(Product::getId)
                                                        .collect(Collectors.toList());

                                        // (c) productImageRepository를 통해 URL 맵 생성
                                        List<Object[]> rows = productImageRepository
                                                        .findThumbnailUrlsByProductIds(prodIds, MediaType.IMAGE);
                                        Map<Long, String> urlMap = rows.stream()
                                                        .collect(Collectors.toMap(
                                                                        row -> (Long) row[0],
                                                                        row -> (String) row[1]));

                                        // (d) DTO 변환
                                        List<FeaturedProductSummaryResponseDTO> summaryList = prods.stream()
                                                        .map(p -> FeaturedProductSummaryResponseDTO.builder()
                                                                        .productId(p.getId())
                                                                        .name(p.getName())
                                                                        .price(p.getPrice())
                                                                        .imageThumbnailUrl(urlMap.get(p.getId()))
                                                                        .build())
                                                        .collect(Collectors.toList());

                                        return FeaturedSellerProductsResponseDTO.builder()
                                                        .sellerId(seller.getSellerId())
                                                        .sellerName(seller.getSellerName())
                                                        .products(summaryList)
                                                        .build();
                                })
                                .collect(Collectors.toList());
        }
}
