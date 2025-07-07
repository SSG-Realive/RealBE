package com.realive.util;

import com.realive.dto.chatbot.ChatRequestDTO;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FunctionSchemaFactory {

    // 특정 주문 상세 조회
    public static ChatRequestDTO.FunctionDefinition getOrderDetailFunction() {
        Map<String, Object> parameters = buildParameters(Map.of(
                "orderId", prop("integer", "주문 번호 ID")
                // "customerId" 제거됨
        ), List.of("orderId"));

        return new ChatRequestDTO.FunctionDefinition(
                "getOrderDetail",
                "특정 주문 ID의 상세 정보를 조회합니다.",
                parameters
        );
    }

    // 주문 내역 조회
    public static ChatRequestDTO.FunctionDefinition getOrderListFunction() {
        Map<String, Object> parameters = buildParameters(Map.of(
                "limit", prop("integer", "조회할 최대 주문 개수 (기본값: 10)")
                // "customerId" 제거됨
        ), List.of()); // 필수 없음

        return new ChatRequestDTO.FunctionDefinition(
                "getOrderList",
                "사용자의 전체 주문 내역을 조회합니다.",
                parameters
        );
    }

    // 작성한 리뷰 목록 조회
    public static ChatRequestDTO.FunctionDefinition getReviewListFunction() {
        Map<String, Object> parameters = buildParameters(Map.of(
                // "customerId" 제거됨 → 파라미터 없음
        ), List.of());

        return new ChatRequestDTO.FunctionDefinition(
                "getReviewList",
                "사용자가 작성한 리뷰 목록을 조회합니다.",
                parameters
        );
    }

    // 찜 목록 조회
    public static ChatRequestDTO.FunctionDefinition getWishlistFunction() {
        Map<String, Object> parameters = buildParameters(Map.of(
                // "customerId" 제거됨 → 파라미터 없음
        ), List.of());

        return new ChatRequestDTO.FunctionDefinition(
                "getWishlistForCustomer",
                "사용자의 찜 목록을 조회합니다.",
                parameters
        );
    }

    // 상품 상세 정보 조회
    public static ChatRequestDTO.FunctionDefinition getProductInfoFunction() {
        Map<String, Object> parameters = buildParameters(Map.of(
                "productId", prop("integer", "상품 ID"),
                "sellerId", prop("integer", "판매자 ID")
        ), List.of("productId", "sellerId"));

        return new ChatRequestDTO.FunctionDefinition(
                "getProductInfo",
                "특정 상품의 상세 정보를 조회합니다.",
                parameters
        );
    }

    // 판매자 정보 조회 (상품 기준)
    public static ChatRequestDTO.FunctionDefinition getSellerInfoByProductFunction() {
        Map<String, Object> parameters = buildParameters(Map.of(
                "productId", prop("integer", "상품 ID")
        ), List.of("productId"));

        return new ChatRequestDTO.FunctionDefinition(
                "getSellerInfoByProduct",
                "상품 ID를 기준으로 판매자 정보를 조회합니다.",
                parameters
        );
    }

    // 추천 셀러와 상품 조회
    public static ChatRequestDTO.FunctionDefinition getFeaturedSellersWithProductsFunction() {
        Map<String, Object> parameters = buildParameters(Map.of(
                "candidateSize", prop("integer", "후보 셀러 수"),
                "sellersPick", prop("integer", "추천할 셀러 수"),
                "productsPerSeller", prop("integer", "셀러당 추천 상품 수"),
                "minReviews", prop("integer", "최소 리뷰 수 기준")
        ), List.of("candidateSize", "sellersPick", "productsPerSeller", "minReviews"));

        return new ChatRequestDTO.FunctionDefinition(
                "getFeaturedSellersWithProducts",
                "리뷰 수를 기준으로 추천 셀러와 추천 상품을 랜덤으로 조회합니다.",
                parameters
        );
    }



    // 상품 검색 (카테고리별, limit 개수)
    public static ChatRequestDTO.FunctionDefinition searchProductsFunction() {
        Map<String, Object> parameters = buildParameters(Map.of(
                "limit", prop("integer", "조회할 최대 상품 개수 (기본값: 10)"),
                "categoryId", prop("integer", "카테고리 ID (선택 사항)")
        ), List.of()); // 필수 없음

        return new ChatRequestDTO.FunctionDefinition(
                "searchProducts",
                "카테고리 기준으로 최대 limit 개수만큼 상품을 조회합니다.",
                parameters
        );
    }

    // 관련 상품 조회 (특정 상품 기준)
    public static ChatRequestDTO.FunctionDefinition getRelatedProductsFunction() {
        Map<String, Object> parameters = buildParameters(Map.of(
                "productId", prop("integer", "기준이 되는 상품 ID")
        ), List.of("productId")); // 필수

        return new ChatRequestDTO.FunctionDefinition(
                "getRelatedProducts",
                "특정 상품을 기준으로 관련 상품 목록을 조회합니다.",
                parameters
        );
    }

    // 인기 상품 조회 (파라미터 없음)
    public static ChatRequestDTO.FunctionDefinition getPopularProductsFunction() {
        Map<String, Object> parameters = buildParameters(Map.of(
                // 파라미터 없음
        ), List.of());

        return new ChatRequestDTO.FunctionDefinition(
                "getPopularProducts",
                "찜이 많은 인기 상품 목록을 조회합니다.",
                parameters
        );
    }

    public static ChatRequestDTO.FunctionDefinition getRecommendedProductsByCategoryFunction() {
        Map<String, Object> parameters = buildParameters(Map.of(
                "categoryId", prop("integer", "카테고리 ID"),
                "limit", prop("integer", "추천 받을 상품 개수 (기본값: 6)")
        ), List.of("categoryId"));  // limit은 선택, categoryId는 필수

        return new ChatRequestDTO.FunctionDefinition(
                "getRecommendedProductsByCategory",
                "특정 카테고리를 기반으로 추천 상품 목록을 조회합니다.",
                parameters
        );
    }

    public static ChatRequestDTO.FunctionDefinition getActiveAuctionsFunction() {
        Map<String, Object> parameters = buildParameters(Map.of(
                "categoryFilter", prop("string", "조회할 경매 상품의 카테고리명 (예: '소파', '침대')"),
                "statusFilter", prop("string", "경매 상태 필터 (예: 'PROCEEDING', 'COMPLETED', 'SCHEDULED')"),
                "limit", prop("integer", "조회할 최대 경매 개수 (기본값: 10)")
        ), List.of()); // 모두 선택

        return new ChatRequestDTO.FunctionDefinition(
                "getActiveAuctions",
                "카테고리와 상태 필터를 기반으로 진행 중인 경매 목록을 조회합니다.",
                parameters
        );
    }

    public static ChatRequestDTO.FunctionDefinition getAuctionDetailsFunction() {
        Map<String, Object> parameters = buildParameters(Map.of(
                "auctionId", prop("integer", "조회할 경매 ID")
        ), List.of("auctionId"));

        return new ChatRequestDTO.FunctionDefinition(
                "getAuctionDetails",
                "특정 경매 ID의 상세 정보를 조회합니다.",
                parameters
        );
    }


    // 모든 조회 가능한 함수 목록 반환
    public static List<ChatRequestDTO.FunctionDefinition> getAllFunctions() {
        return List.of(
                getOrderDetailFunction(),
                getOrderListFunction(),
                getReviewListFunction(),
                getWishlistFunction(),
                getPopularProductsFunction(),
                getProductInfoFunction(),
                getSellerInfoByProductFunction(),
                getFeaturedSellersWithProductsFunction(),
                getRecommendedProductsByCategoryFunction(),
                getRelatedProductsFunction(),
                searchProductsFunction(),
                getActiveAuctionsFunction(),
                getAuctionDetailsFunction()
        );
    }
    // ====== 공통 로직 추출 ======
    private static Map<String, String> prop(String type, String desc) {
        Map<String, String> map = new HashMap<>();
        map.put("type", type);
        map.put("description", desc);
        return map;
    }

    private static Map<String, Object> buildParameters(Map<String, Map<String, String>> properties, List<String> required) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("type", "object");
        parameters.put("properties", properties);
        parameters.put("required", required);
        return parameters;
    }
}
