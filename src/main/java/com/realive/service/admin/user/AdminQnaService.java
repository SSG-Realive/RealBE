package com.realive.service.admin.user;

import com.realive.dto.sellerqna.SellerQnaDetailResponseDTO;
import com.realive.dto.sellerqna.SellerQnaResponseDTO;
import com.realive.dto.sellerqna.SellerQnaStatisticsDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface AdminQnaService {
    // 판매자 Q&A 목록 조회 (페이징)
    Page<SellerQnaResponseDTO> getAllSellerQnaList(Pageable pageable);

    // 특정 판매자 Q&A 상세 조회
    SellerQnaDetailResponseDTO getSellerQnaDetail(Long qnaId);

    // 관리자가 판매자 Q&A에 답변 등록/수정
    void answerSellerQna(Long qnaId, String answer);

    // 판매자 Q&A 삭제
    void deleteSellerQna(Long qnaId);

    // 통계 조회
    SellerQnaStatisticsDTO getSellerQnaStatistics();
}