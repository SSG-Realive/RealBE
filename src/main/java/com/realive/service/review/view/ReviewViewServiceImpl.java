package com.realive.service.review.view;

import com.realive.dto.review.MyReviewResponseDTO;
import com.realive.dto.review.ReviewListResponseDTO;
import com.realive.dto.review.ReviewResponseDTO;
import com.realive.repository.review.view.ReviewViewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
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

        // 이미지 조회
        Map<Long, List<String>> reviewImageUrlsMap = reviewViewRepository.findImageUrlsByReviewIds(reviewIds)
                .stream()
                .collect(Collectors.groupingBy(
                        tuple -> (Long) tuple[0],
                        Collectors.mapping(tuple -> (String) tuple[1], Collectors.toList())
                ));

        // 리뷰별로 상품명 조회 (orderId + sellerId 기반)
        Map<Long, List<String>> productNamesMap = new HashMap<>();
        reviewsPage.getContent().forEach(reviewDto -> {
            List<Object[]> productNames = reviewViewRepository.findProductNamesByOrderIdsAndSellerId(
                    List.of(reviewDto.getOrderId()), reviewDto.getSellerId()
            );
            List<String> names = productNames.stream()
                    .map(tuple -> (String) tuple[1])
                    .collect(Collectors.toList());
            productNamesMap.put(reviewDto.getReviewId(), names);
        });

        reviewsPage.getContent().forEach(reviewDto -> {
            reviewDto.setImageUrls(reviewImageUrlsMap.getOrDefault(reviewDto.getReviewId(), List.of()));
            reviewDto.setProductName(
                    summarizeProductNames(productNamesMap.getOrDefault(reviewDto.getReviewId(), List.of()))
            );
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

            if (reviewDto.getOrderId() != null && reviewDto.getSellerId() != null) {
                List<Object[]> productNames = reviewViewRepository.findProductNamesByOrderIdsAndSellerId(
                        List.of(reviewDto.getOrderId()), reviewDto.getSellerId()
                );
                List<String> names = productNames.stream()
                        .map(tuple -> (String) tuple[1])
                        .collect(Collectors.toList());
                reviewDto.setProductName(summarizeProductNames(names));
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

        Map<Long, List<String>> reviewImageUrlsMap = reviewViewRepository.findImageUrlsByReviewIds(reviewIds)
                .stream()
                .collect(Collectors.groupingBy(
                        tuple -> (Long) tuple[0],
                        Collectors.mapping(tuple -> (String) tuple[1], Collectors.toList())
                ));

        Map<Long, List<String>> productNamesMap = new HashMap<>();
        myReviewsPage.getContent().forEach(reviewDto -> {
            List<Object[]> productNames = reviewViewRepository.findProductNamesByOrderIdsAndSellerId(
                    List.of(reviewDto.getOrderId()), reviewDto.getSellerId()
            );
            List<String> names = productNames.stream()
                    .map(tuple -> (String) tuple[1])
                    .collect(Collectors.toList());
            productNamesMap.put(reviewDto.getReviewId(), names);
        });

        myReviewsPage.getContent().forEach(reviewDto -> {
            reviewDto.setImageUrls(reviewImageUrlsMap.getOrDefault(reviewDto.getReviewId(), List.of()));
            reviewDto.setProductName(
                    summarizeProductNames(productNamesMap.getOrDefault(reviewDto.getReviewId(), List.of()))
            );
        });

        log.info("총 {}개의 내 리뷰를 조회했습니다.", myReviewsPage.getTotalElements());
        return myReviewsPage;
    }

    // 상품명이 여러 개일 경우 "첫 상품 외 N개" 형태로 요약
    private String summarizeProductNames(List<String> names) {
        if (names == null || names.isEmpty()) return null;
        if (names.size() == 1) return names.get(0);
        return names.get(0) + " 외 " + (names.size() - 1) + "개";
    }
}
