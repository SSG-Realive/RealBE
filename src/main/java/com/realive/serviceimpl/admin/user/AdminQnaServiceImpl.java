package com.realive.serviceimpl.admin.user;

import com.realive.domain.seller.SellerQna;
import com.realive.dto.sellerqna.SellerQnaDetailResponseDTO;
import com.realive.dto.sellerqna.SellerQnaResponseDTO;
import com.realive.dto.sellerqna.SellerQnaStatisticsDTO;
import com.realive.repository.seller.SellerQnaRepository;
import com.realive.service.admin.user.AdminQnaService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminQnaServiceImpl implements AdminQnaService {

    private final SellerQnaRepository sellerQnaRepository; // CustomerQnaRepository 대신

    @Override
    public Page<SellerQnaResponseDTO> getAllSellerQnaList(Pageable pageable) {
        Page<SellerQna> qnaPage = sellerQnaRepository.findAll(pageable);
        return qnaPage.map(qna -> SellerQnaResponseDTO.builder()
                .id(qna.getId())
                .title(qna.getTitle())
                .content(qna.getContent())
                .answer(qna.getAnswer())
                .isAnswered(qna.isAnswered())
                .createdAt(qna.getCreatedAt())
                .updatedAt(qna.getUpdatedAt())
                .answeredAt(qna.getAnsweredAt())
                .build());
    }

    @Override
    public SellerQnaDetailResponseDTO getSellerQnaDetail(Long qnaId) {
        SellerQna qna = sellerQnaRepository.findById(qnaId)
                .orElseThrow(() -> new EntityNotFoundException("Q&A를 찾을 수 없습니다."));

        return SellerQnaDetailResponseDTO.builder()
                .id(qna.getId())
                .title(qna.getTitle())
                .content(qna.getContent())
                .answer(qna.getAnswer())
                .isAnswered(qna.isAnswered())
                .createdAt(qna.getCreatedAt())
                .updatedAt(qna.getUpdatedAt())
                .answeredAt(qna.getAnsweredAt())
                .sellerId(qna.getSeller().getId())
                .sellerName(qna.getSeller().getName())
                .sellerEmail(qna.getSeller().getEmail())
                .build();
    }

    @Override
    @Transactional
    public void answerSellerQna(Long qnaId, String answer) {
        SellerQna qna = sellerQnaRepository.findById(qnaId)
                .orElseThrow(() -> new EntityNotFoundException("Q&A를 찾을 수 없습니다."));

        qna.setAnswer(answer);
        qna.setAnswered(true);
        qna.setAnsweredAt(LocalDateTime.now());

        sellerQnaRepository.save(qna);
    }

    @Override
    @Transactional
    public void deleteSellerQna(Long qnaId) {
        SellerQna qna = sellerQnaRepository.findById(qnaId)
                .orElseThrow(() -> new EntityNotFoundException("Q&A를 찾을 수 없습니다."));

        sellerQnaRepository.delete(qna);
    }

    @Override
    public SellerQnaStatisticsDTO getSellerQnaStatistics() {
        long totalCount = sellerQnaRepository.count();
        long answeredCount = sellerQnaRepository.countByIsAnsweredTrue();
        long unansweredCount = totalCount - answeredCount;
        double answerRate = totalCount > 0 ? (double) answeredCount / totalCount * 100 : 0.0;

        return SellerQnaStatisticsDTO.builder()
                .totalCount(totalCount)
                .answeredCount(answeredCount)
                .unansweredCount(unansweredCount)
                .answerRate(answerRate)
                .build();
    }
}