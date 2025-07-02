package com.realive.controller.seller;

import com.realive.dto.sellerqna.*;
import com.realive.security.seller.SellerPrincipal;
import com.realive.service.seller.SellerQnaService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/seller/adminqna")
public class SellerAdminQnaController {

    private final SellerQnaService sellerQnaService;

    // ✅ QnA 작성
    @PostMapping("/new")
    public ResponseEntity<Void> createQna(@RequestBody SellerQnaRequestDTO dto, @AuthenticationPrincipal SellerPrincipal principal) {

        sellerQnaService.createQna(principal.getId(), dto);
        return ResponseEntity.ok().build();
    }

    // ✅ QnA 목록 조회 (검색 기능 추가)
    @GetMapping
    public ResponseEntity<Page<SellerQnaResponseDTO>> getQnaList(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam(required = false) String keyword,  // 🔥 검색 키워드 추가
            @AuthenticationPrincipal SellerPrincipal principal) {

        Page<SellerQnaResponseDTO> qnaList = sellerQnaService.getQnaListBySellerId(principal.getId(), pageable);
        return ResponseEntity.ok(qnaList);
    }

    // ✅ QnA 단건 조회
    @GetMapping("/{qnaId}")
    public ResponseEntity<SellerQnaDetailResponseDTO> getQnaDetail(@PathVariable Long qnaId, @AuthenticationPrincipal SellerPrincipal principal) {

        SellerQnaDetailResponseDTO detail = sellerQnaService.getQnaDetail(principal.getId(), qnaId);
        return ResponseEntity.ok(detail);
    }

    // ✅ QnA 수정 (답변 전)
    @PutMapping("/{qnaId}/edit")
    public ResponseEntity<Void> updateQna(
            @PathVariable Long qnaId,
            @RequestBody SellerQnaUpdateRequestDTO dto,
            @AuthenticationPrincipal SellerPrincipal principal) {

        sellerQnaService.updateQnaContent(principal.getId(), qnaId, dto);
        return ResponseEntity.ok().build();
    }

    // ✅ QnA 삭제 (soft delete)
    @PatchMapping("/{qnaId}/edit")
    public ResponseEntity<Void> deleteQna(@PathVariable Long qnaId, @AuthenticationPrincipal SellerPrincipal principal) {

        sellerQnaService.deleteQna(principal.getId(), qnaId);
        return ResponseEntity.ok().build();
    }
}