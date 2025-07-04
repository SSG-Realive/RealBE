package com.realive.util;

import com.realive.dto.chatbot.ChatRequestDTO;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FunctionSchemaFactory {

    // 특정 주문 상세 조회
    public static ChatRequestDTO.FunctionDefinition getOrderDetailFunction() {
        Map<String, Object> parameters = buildParameters(Map.of(
                "orderId", prop("integer", "주문 번호 ID"),
                "customerId", prop("integer", "사용자 ID")
        ), List.of("orderId", "customerId"));

        return new ChatRequestDTO.FunctionDefinition(
                "getOrderDetail",
                "특정 주문 ID의 상세 정보를 조회합니다.",
                parameters
        );
    }

    // 주문 내역 조회
    public static ChatRequestDTO.FunctionDefinition getOrderListFunction() {
        Map<String, Object> parameters = buildParameters(Map.of(
                "customerId", prop("integer", "사용자 ID"),
                "limit", prop("integer", "조회할 최대 주문 개수 (기본값: 10)")
        ), List.of("customerId"));

        return new ChatRequestDTO.FunctionDefinition(
                "getOrderList",
                "사용자의 전체 주문 내역을 조회합니다.",
                parameters
        );
    }

    // 작성한 리뷰 목록 조회
    public static ChatRequestDTO.FunctionDefinition getReviewListFunction() {
        Map<String, Object> parameters = buildParameters(Map.of(
                "customerId", prop("integer", "사용자 ID")
        ), List.of("customerId"));

        return new ChatRequestDTO.FunctionDefinition(
                "getReviewList",
                "사용자가 작성한 리뷰 목록을 조회합니다.",
                parameters
        );
    }

    // 찜 목록 조회
    public static ChatRequestDTO.FunctionDefinition getWishlistFunction() {
        Map<String, Object> parameters = buildParameters(Map.of(
                "customerId", prop("integer", "사용자 ID")
        ), List.of("customerId"));

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
    public static ChatRequestDTO.FunctionDefinition getFeaturedSellersFunction() {
        Map<String, Object> parameters = buildParameters(Map.of(
                "candidateSize", prop("integer", "후보 셀러 수"),
                "sellersPick", prop("integer", "추천할 셀러 수"),
                "productsPerSeller", prop("integer", "셀러당 추천 상품 수"),
                "minReviews", prop("integer", "최소 리뷰 수 기준")
        ), List.of("candidateSize", "sellersPick", "productsPerSeller", "minReviews"));

        return new ChatRequestDTO.FunctionDefinition(
                "getFeaturedSellers",
                "리뷰 수를 기준으로 추천 셀러와 추천 상품을 랜덤으로 조회합니다.",
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
                getProductInfoFunction(),
                getSellerInfoByProductFunction(),
                getFeaturedSellersFunction()
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
