package com.realive.repository.seller;

import com.realive.domain.seller.SellerQna;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.nio.channels.FileChannel;
import java.util.Optional;

public interface SellerQnaRepository extends JpaRepository<SellerQna, Long> {

    // 일반 목록 조회 (마이페이지 등)
    Page<SellerQna> findBySellerIdAndIsActiveTrue(Long sellerId, Pageable pageable);

    // 관리자 전체 목록 조회
    Page<SellerQna> findBySellerId(Long sellerId, Pageable pageable);

    // 단건 조회 (판매자 본인 확인)
    Optional<SellerQna> findByIdAndSellerIdAndIsActiveTrue(Long id, Long sellerId);

    // 미답변 필터링 조회
    Page<SellerQna> findBySellerIdAndIsAnsweredFalseAndIsActiveTrue(Long sellerId, Pageable pageable);

    // 미답변 QnA 수
    long countBySellerIdAndIsAnsweredFalseAndIsActiveTrue(Long sellerId);

    // 총 QnA 수
    long countBySellerIdAndIsActiveTrue(Long sellerId);

    // ✅ 검색 기능을 위한 메서드 추가
    Page<SellerQna> findBySellerIdAndIsActiveTrueAndTitleContainingOrContentContaining(
            Long sellerId, String titleKeyword, String contentKeyword, Pageable pageable);

    // 또는 더 간단하게 (제목 또는 내용에 키워드 포함)
    @Query("SELECT sq FROM SellerQna sq WHERE sq.seller.id = :sellerId AND sq.isActive = true " +
            "AND (sq.title LIKE %:keyword% OR sq.content LIKE %:keyword%)")
    Page<SellerQna> findBySellerIdAndKeyword(@Param("sellerId") Long sellerId,
                                             @Param("keyword") String keyword,
                                             Pageable pageable);
}