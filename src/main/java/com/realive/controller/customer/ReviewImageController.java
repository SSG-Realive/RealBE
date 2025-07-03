package com.realive.controller.customer;

import com.realive.service.review.ReviewImageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/customer/reviews/images")
@RequiredArgsConstructor
public class ReviewImageController {

    private final ReviewImageService reviewImageService;

    /**
     * 리뷰 이미지 업로드
     */
    @PostMapping("/upload")
    public ResponseEntity<List<String>> uploadImages(
            @RequestParam Long reviewId,
            @RequestParam(value = "files", required = false) List<MultipartFile> files) {

        List<String> uploadedUrls = reviewImageService.uploadImages(reviewId, files);
        return ResponseEntity.ok(uploadedUrls);
    }

    /**
     * 리뷰 이미지 전체 삭제
     * @param reviewId 리뷰 ID
     * @return 삭제 완료 메시지
     */
    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteImages(@RequestParam Long reviewId) {
        reviewImageService.deleteImagesByReviewId(reviewId);
        return ResponseEntity.ok("리뷰 이미지가 삭제되었습니다.");
    }
}