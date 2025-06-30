package com.realive.dto.seller;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SellerPublicResponseDTO {

    private Long id;                        // 판매자 ID (식별용)
    private String name;                    // 판매자의 공개 이름 또는 업체명
    // private String profileImageUrl;         // 판매자 프로필 이미지 URL (Seller 엔티티에 해당 필드가 있다고 가정)
    //private boolean isApproved;             // 승인 여부 (이 판매자가 신뢰할 수 있는지 보여주는 정보)
    private double averageRating;        // (선택 사항) 판매자의 평균 평점 등 신뢰도 관련 정보
    private Long totalReviews;            // (선택 사항) 총 리뷰 수

}