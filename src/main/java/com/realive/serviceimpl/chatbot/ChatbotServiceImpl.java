package com.realive.serviceimpl.chatbot;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.realive.dto.chatbot.ChatApiResponseDTO;
import com.realive.dto.chatbot.ChatRequestDTO;
import com.realive.dto.page.PageRequestDTO;
import com.realive.security.customer.CustomerPrincipal;
import com.realive.service.chatbot.ChatbotService;
import com.realive.service.customer.ProductViewService;
import com.realive.service.customer.WishlistService;
import com.realive.service.order.OrderService;
import com.realive.service.product.ProductService;
import com.realive.service.review.view.ReviewViewService;
import com.realive.util.FunctionSchemaFactory;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Map;

@Service
@Log4j2
public class ChatbotServiceImpl implements ChatbotService {

    @Qualifier("openAiWebClient")
    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    private final ProductService productService;
    private final OrderService orderService;
    private final ReviewViewService reviewViewService;
    private final WishlistService wishlistService;
    private final ProductViewService productViewService;

    @Value("${openai.api.key}")
    private String openAiApiKey;

    @Value("${openai.model}")
    private String model;

    // ✅ 키워드 기반 카테고리 ID 매핑
    private static final Map<String, Long> CATEGORY_KEYWORD_MAP = Map.ofEntries(
            Map.entry("거실 가구", 10L),
            Map.entry("침실 가구", 20L),
            Map.entry("주방·다이닝 가구", 30L),
            Map.entry("서재·오피스 가구", 40L),
            Map.entry("기타 가구", 50L),
            Map.entry("소파", 11L),
            Map.entry("거실 테이블", 12L),
            Map.entry("TV·미디어장", 13L),
            Map.entry("진열장·책장", 14L),
            Map.entry("침대", 21L),
            Map.entry("매트리스", 22L),
            Map.entry("화장대·거울", 23L),
            Map.entry("옷장·행거", 24L),
            Map.entry("수납장·서랍장", 25L),
            Map.entry("식탁", 31L),
            Map.entry("주방 의자", 32L),
            Map.entry("주방 수납장", 33L),
            Map.entry("아일랜드 식탁·홈바", 34L),
            Map.entry("책상", 41L),
            Map.entry("사무용 의자", 42L),
            Map.entry("책장", 43L),
            Map.entry("현관·중문 가구", 51L),
            Map.entry("야외·아웃도어 가구", 52L),
            Map.entry("리퍼·전시가구", 53L),
            Map.entry("DIY·부속품", 54L)
    );

    private Long resolveCategoryIdFromKeyword(String userMessage) {
        for (String keyword : CATEGORY_KEYWORD_MAP.keySet()) {
            if (userMessage.contains(keyword)) {
                return CATEGORY_KEYWORD_MAP.get(keyword);
            }
        }
        return null;
    }

    private Long getCurrentCustomerId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new RuntimeException("로그인한 사용자만 사용할 수 있는 기능입니다.");
        }
        CustomerPrincipal userDetails = (CustomerPrincipal) auth.getPrincipal();
        return userDetails.getId();
    }

    public ChatbotServiceImpl(@Qualifier("openAiWebClient") WebClient webClient,
                              ObjectMapper objectMapper,
                              ProductService productService,
                              OrderService orderService,
                              ReviewViewService reviewViewService,
                              WishlistService wishlistService,
                              ProductViewService productViewService) {
        this.webClient = webClient;
        this.objectMapper = objectMapper;
        this.productService = productService;
        this.orderService = orderService;
        this.reviewViewService = reviewViewService;
        this.wishlistService = wishlistService;
        this.productViewService = productViewService;
        log.info("[ChatbotServiceImpl] WebClient 빈 확인: {}", webClient);
    }

    @Override
    public String getChatbotReply(String message) {
        try {
            // ✅ 사용자 메시지에서 카테고리 ID 추출 시도
            Long categoryId = resolveCategoryIdFromKeyword(message);
            if (categoryId != null) {
                log.info("사용자 메시지에서 카테고리 ID를 찾음: {}", categoryId);

                // ✅ GPT 호출 없이 직접 함수 실행 (선제 실행 방식)
                var recommendedProducts = productViewService.getRecommendedProductsByCategory(categoryId, 2); // 2개만 추천
                String functionResult = objectMapper.writeValueAsString(recommendedProducts);

                // ✅ GPT에게 함수 결과를 넘겨서 자연어 응답 받기
                ChatRequestDTO followupRequest = new ChatRequestDTO();
                followupRequest.setModel(model);
                followupRequest.setMessages(List.of(
                        new ChatRequestDTO.Message("user", message),
                        new ChatRequestDTO.Message("function", functionResult, "getRecommendedProductsByCategory")
                ));
                followupRequest.setFunctions(FunctionSchemaFactory.getAllFunctions());

                String followupRequestJson = objectMapper.writeValueAsString(followupRequest);
                log.info("OpenAI 후속 요청 JSON (카테고리 인식 경로): {}", followupRequestJson);

                String followupResponseJson = webClient.post()
                        .uri("https://api.openai.com/v1/chat/completions")
                        .header("Authorization", "Bearer " + openAiApiKey)
                        .header("Content-Type", "application/json")
                        .bodyValue(followupRequestJson)
                        .retrieve()
                        .bodyToMono(String.class)
                        .block();

                ChatApiResponseDTO followupResponse = objectMapper.readValue(followupResponseJson, ChatApiResponseDTO.class);
                return followupResponse.getChoices().get(0).getMessage().getContent();
            } else {
                log.info("사용자 메시지에서 카테고리 ID를 찾지 못함");
            }

            // ✅ 함수 호출 가능한 일반 GPT 요청
            ChatRequestDTO request = ChatRequestDTO.withFunctions(
                    model,
                    message,
                    FunctionSchemaFactory.getAllFunctions()
            );

            String requestJson = objectMapper.writeValueAsString(request);
            log.info("OpenAI 요청 JSON: {}", requestJson);

            String responseJson = webClient.post()
                    .uri("https://api.openai.com/v1/chat/completions")
                    .header("Authorization", "Bearer " + openAiApiKey)
                    .header("Content-Type", "application/json")
                    .bodyValue(requestJson)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode root = objectMapper.readTree(responseJson);
            JsonNode choice = root.path("choices").get(0);
            JsonNode messageNode = choice.path("message");

            if (messageNode.has("function_call")) {
                String functionName = messageNode.get("function_call").get("name").asText();
                String argumentsJson = messageNode.get("function_call").get("arguments").asText();
                String functionResult = handleFunctionCall(functionName, argumentsJson);

                ChatRequestDTO followupRequest = new ChatRequestDTO();
                followupRequest.setModel(model);
                followupRequest.setMessages(List.of(
                        new ChatRequestDTO.Message("user", message),
                        new ChatRequestDTO.Message("function", functionResult, functionName)
                ));
                followupRequest.setFunctions(FunctionSchemaFactory.getAllFunctions());

                String followupRequestJson = objectMapper.writeValueAsString(followupRequest);
                log.info("OpenAI 후속 요청 JSON: {}", followupRequestJson);

                String followupResponseJson = webClient.post()
                        .uri("https://api.openai.com/v1/chat/completions")
                        .header("Authorization", "Bearer " + openAiApiKey)
                        .header("Content-Type", "application/json")
                        .bodyValue(followupRequestJson)
                        .retrieve()
                        .bodyToMono(String.class)
                        .block();

                ChatApiResponseDTO followupResponse = objectMapper.readValue(followupResponseJson, ChatApiResponseDTO.class);
                return followupResponse.getChoices().get(0).getMessage().getContent();
            } else {
                ChatApiResponseDTO response = objectMapper.readValue(responseJson, ChatApiResponseDTO.class);
                return response.getChoices().get(0).getMessage().getContent();
            }

        } catch (Exception e) {
            e.printStackTrace();
            return "⚠️ 챗봇 응답 처리 중 오류 발생";
        }
    }

    // 이하 함수 호출 처리 메서드는 그대로 유지
    private String handleFunctionCall(String functionName, String argumentsJson) {
        try {
            JsonNode argsNode = objectMapper.readTree(argumentsJson);

            switch (functionName) {
                case "getOrderDetail":
                    Long orderId = argsNode.get("orderId").asLong();
                    Long customerId = getCurrentCustomerId();
                    return objectMapper.writeValueAsString(orderService.getOrder(orderId, customerId));

                case "getOrderList":
                    customerId = getCurrentCustomerId();
                    int limit = argsNode.has("limit") ? argsNode.get("limit").asInt() : 10;
                    Pageable pageable = PageRequest.of(0, limit);
                    return objectMapper.writeValueAsString(orderService.getOrderList(pageable, customerId));

                case "getReviewList":
                    customerId = getCurrentCustomerId();
                    limit = argsNode.has("limit") ? argsNode.get("limit").asInt() : 10;
                    pageable = PageRequest.of(0, limit);
                    return objectMapper.writeValueAsString(reviewViewService.getReviewList(customerId, pageable));

                case "getWishlistForCustomer":
                    customerId = getCurrentCustomerId();
                    return objectMapper.writeValueAsString(wishlistService.getWishlistForCustomer(customerId));

                case "getProductInfo":
                    Long productId = argsNode.get("productId").asLong();
                    Long sellerId = argsNode.has("sellerId") && !argsNode.get("sellerId").isNull()
                            ? argsNode.get("sellerId").asLong() : null;
                    return objectMapper.writeValueAsString(productService.getProductDetail(productId, sellerId));

                case "getPublicSellerInfoByProductId":
                    productId = argsNode.get("productId").asLong();
                    return objectMapper.writeValueAsString(productService.getPublicSellerInfoByProductId(productId));

                case "getFeaturedSellersWithProducts":
                    int candidateSize = argsNode.get("candidateSize").asInt();
                    int sellersPick = argsNode.get("sellersPick").asInt();
                    int productsPerSeller = argsNode.get("productsPerSeller").asInt();
                    int minReviews = argsNode.get("minReviews").asInt();
                    return objectMapper.writeValueAsString(productService.getFeaturedSellersWithProducts(candidateSize, sellersPick, productsPerSeller, minReviews));

                case "searchProducts":
                    limit = argsNode.has("limit") ? argsNode.get("limit").asInt() : 10;
                    Long categoryId = argsNode.has("categoryId") && !argsNode.get("categoryId").isNull()
                            ? argsNode.get("categoryId").asLong() : null;
                    PageRequestDTO pageRequestDTO = new PageRequestDTO();
                    pageRequestDTO.setPage(1);
                    pageRequestDTO.setSize(limit);
                    return objectMapper.writeValueAsString(productViewService.search(pageRequestDTO, categoryId));

                case "getRelatedProducts":
                    productId = argsNode.get("productId").asLong();
                    return objectMapper.writeValueAsString(productViewService.getRelatedProducts(productId));

                case "getPopularProducts":
                    return objectMapper.writeValueAsString(productViewService.getPopularProducts());

                case "getRecommendedProductsByCategory":
                    categoryId = argsNode.get("categoryId").asLong();
                    limit = argsNode.has("limit") ? argsNode.get("limit").asInt() : 6;
                    return objectMapper.writeValueAsString(productViewService.getRecommendedProductsByCategory(categoryId, limit));

                default:
                    return "{\"error\": \"알 수 없는 함수 호출: " + functionName + "\"}";
            }

        } catch (Exception e) {
            e.printStackTrace();
            return "{\"error\": \"함수 호출 처리 중 예외 발생: " + e.getMessage() + "\"}";
        }
    }
}
