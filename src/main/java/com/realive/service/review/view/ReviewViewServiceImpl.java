package com.realive.service.review.view;

import com.realive.dto.page.PageRequestDTO;
import com.realive.dto.page.PageResponseDTO;
import com.realive.dto.review.MyReviewResponseDTO;
import com.realive.dto.review.ReviewListResponseDTO;
import com.realive.dto.review.ReviewResponseDTO;
import com.realive.repository.review.view.ReviewViewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Log4j2
@Transactional(readOnly = true)
public class ReviewViewServiceImpl implements ReviewViewService {

    private final ReviewViewRepository reviewViewRepository;

    // 판매자 리뷰 목록
    @Override
    public ReviewListResponseDTO getReviewList(Long sellerId, Pageable pageable) {
        log.info("판매자 ID {}에 대한 리뷰 목록을 조회합니다. 페이지 번호: {}", sellerId, pageable.getPageNumber());

        if (sellerId == null || sellerId <= 0) {
            throw new IllegalArgumentException("유효하지 않은 판매자 ID입니다.");
        }

        Page<ReviewResponseDTO> reviewsPage = reviewViewRepository.findSellerReviewsBySellerId(sellerId, pageable);

        List<Long> reviewIds = reviewsPage.getContent().stream()
                .map(ReviewResponseDTO::getReviewId)
                .collect(Collectors.toList());

        List<Long> orderIds = reviewsPage.getContent().stream()
                .map(ReviewResponseDTO::getOrderId)
                .collect(Collectors.toList());

        // 이미지 조회
        Map<Long, List<String>> reviewImageUrlsMap = reviewViewRepository.findImageUrlsByReviewIds(reviewIds)
                .stream()
                .collect(Collectors.groupingBy(
                        tuple -> (Long) tuple[0],
                        Collectors.mapping(tuple -> (String) tuple[1], Collectors.toList())
                ));

        // 상품명 조회
        Map<Long, String> productNamesMap = reviewViewRepository.findProductNamesByOrderIds(orderIds)
                .stream()
                .collect(Collectors.toMap(
                        tuple -> (Long) tuple[0], // orderId
                        tuple -> (String) tuple[1] // productName
                ));

        // 리뷰 DTO에 세팅
        reviewsPage.getContent().forEach(reviewDto -> {
            reviewDto.setImageUrls(reviewImageUrlsMap.getOrDefault(reviewDto.getReviewId(), List.of()));
            if (reviewDto.getOrderId() != null) {
                reviewDto.setProductName(productNamesMap.get(reviewDto.getOrderId()));
            }
        });

        log.info("판매자 ID {}에 대한 총 {}개의 리뷰를 조회했습니다.", sellerId, reviewsPage.getTotalElements());

        return ReviewListResponseDTO.builder()
                .reviews(reviewsPage.getContent())
                .totalCount(reviewsPage.getTotalElements())
                .page(reviewsPage.getNumber())
                .size(reviewsPage.getSize())
                .build();
    }

    // 리뷰 상세
    @Override
    public ReviewResponseDTO getReviewDetail(Long id) {
        log.info("리뷰 상세 조회: ID={}", id);

        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid review ID");
        }

        Optional<ReviewResponseDTO> reviewOpt = reviewViewRepository.findReviewDetailById(id);

        reviewOpt.ifPresent(reviewDto -> {
            List<String> imageUrls = reviewViewRepository.findImageUrlsByReviewIds(List.of(reviewDto.getReviewId()))
                    .stream()
                    .map(tuple -> (String) tuple[1])
                    .collect(Collectors.toList());
            reviewDto.setImageUrls(imageUrls);

            if (reviewDto.getOrderId() != null) {
                reviewViewRepository.findProductNamesByOrderIds(List.of(reviewDto.getOrderId()))
                        .stream().findFirst()
                        .ifPresent(tuple -> reviewDto.setProductName((String) tuple[1]));
            }
        });

        return reviewOpt.orElseThrow(() -> new IllegalArgumentException("Review not found with id: " + id));
    }

    // 내가 작성한 리뷰 목록
    @Override
    public Page<MyReviewResponseDTO> getMyReviewList(Long customerId, Pageable pageable) {
        log.info("내 리뷰 목록 조회: customerId={}, page={}", customerId, pageable.getPageNumber());

        if (customerId == null || customerId <= 0) {
            throw new IllegalArgumentException("Invalid customer ID");
        }

        Page<MyReviewResponseDTO> myReviewsPage = reviewViewRepository.findMyReviewsByCustomerId(customerId, pageable);

        List<Long> reviewIds = myReviewsPage.getContent().stream()
                .map(MyReviewResponseDTO::getReviewId)
                .collect(Collectors.toList());

        List<Long> orderIds = myReviewsPage.getContent().stream()
                .map(MyReviewResponseDTO::getOrderId)
                .collect(Collectors.toList());

        Map<Long, List<String>> reviewImageUrlsMap = reviewViewRepository.findImageUrlsByReviewIds(reviewIds)
                .stream()
                .collect(Collectors.groupingBy(
                        tuple -> (Long) tuple[0],
                        Collectors.mapping(tuple -> (String) tuple[1], Collectors.toList())
                ));

        Map<Long, String> productNamesMap = reviewViewRepository.findProductNamesByOrderIds(orderIds)
                .stream()
                .collect(Collectors.toMap(
                        tuple -> (Long) tuple[0],
                        tuple -> (String) tuple[1]
                ));

        myReviewsPage.getContent().forEach(reviewDto -> {
            reviewDto.setImageUrls(reviewImageUrlsMap.getOrDefault(reviewDto.getReviewId(), List.of()));
            if (reviewDto.getOrderId() != null) {
                reviewDto.setProductName(productNamesMap.get(reviewDto.getOrderId()));
            }
        });

        log.info("총 {}개의 내 리뷰를 조회했습니다.", myReviewsPage.getTotalElements());
        return myReviewsPage;
    }

    @Override
    public PageResponseDTO<ReviewResponseDTO> getSellerReviews(Long sellerId, Pageable pageable) {
        log.info("판매자 ID {} 에 대한 리뷰를 페이지 {} 로 가져오는 중입니다.", sellerId, pageable.getPageNumber());

        Page<ReviewResponseDTO> reviewsPage = reviewViewRepository.findSellerReviewsBySellerId(sellerId, pageable);

        // Pageable 객체에서 PageRequestDTO 생성
        // Pageable의 getPageNumber()는 0-based 이므로, PageRequestDTO의 page(1-based)로 변환 시 +1
        // 정렬 정보는 Pageable에서 직접 PageRequestDTO의 sort, direction 필드로 매핑
        PageRequestDTO pageRequestDTO = PageRequestDTO.builder()
                .page(pageable.getPageNumber() + 1) // 0-based -> 1-based
                .size(pageable.getPageSize())
                // Pageable의 Sort 객체에서 정렬 기준 필드와 방향을 추출
                .sort(pageable.getSort().stream()
                        .map(Sort.Order::getProperty)
                        .findFirst().orElse("createdAt")) // 기본 정렬 필드
                .direction(pageable.getSort().stream()
                        .map(order -> order.getDirection().name()) // ASC/DESC 문자열로 변환
                        .findFirst().orElse("DESC")) // 기본 정렬 방향
                .build();

        // PageResponseDTO의 withAll 빌더 메서드를 사용하여 생성합니다.
        return PageResponseDTO.<ReviewResponseDTO>withAll()
                .pageRequestDTO(pageRequestDTO)            // PageRequestDTO 객체 전달
                .dtoList(reviewsPage.getContent())       // 실제 데이터 리스트
                .total( (int) reviewsPage.getTotalElements()) // 전체 데이터 개수 (int로 캐스팅)
                .build();
    }
}
