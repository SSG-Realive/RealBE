package com.realive.serviceimpl.chatbot;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.realive.dto.chatbot.ChatApiResponseDTO;
import com.realive.dto.chatbot.ChatRequestDTO;
import com.realive.dto.page.PageRequestDTO;
import com.realive.dto.product.GenerateProductDescriptionRequestDTO;
import com.realive.security.customer.CustomerPrincipal;
import com.realive.service.admin.auction.AuctionService;
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

import java.util.ArrayList;
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
    private final AuctionService auctionService;

    @Value("${openai.api.key}")
    private String openAiApiKey;

    @Value("${openai.model}")
    private String model;

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
                              ProductViewService productViewService,
                              AuctionService auctionService) {
        this.webClient = webClient;
        this.objectMapper = objectMapper;
        this.productService = productService;
        this.orderService = orderService;
        this.reviewViewService = reviewViewService;
        this.wishlistService = wishlistService;
        this.productViewService = productViewService;
        this.auctionService = auctionService;
        log.info("[ChatbotServiceImpl] WebClient 빈 확인: {}", webClient);
    }

    @Override
    public String getChatbotReply(String message) {
        boolean functionCalled = false;

        try {
            // 1. 기본 GPT 요청 + Function 목록 포함
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

            // 2. GPT가 함수 호출 요청한 경우 처리
            if (messageNode.has("function_call") && !functionCalled) {
                functionCalled = true;
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
                return formatChatbotResponse(followupResponse.getChoices().get(0).getMessage().getContent());
            } else {
                // 함수 호출이 없거나 이미 처리됨 → 일반 응답 처리
                ChatApiResponseDTO response = objectMapper.readValue(responseJson, ChatApiResponseDTO.class);
                return formatChatbotResponse(response.getChoices().get(0).getMessage().getContent());
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

                case "getOrderDetail": {
                    Long orderId = argsNode.get("orderId").asLong();
                    Long customerId = getCurrentCustomerId();
                    return objectMapper.writeValueAsString(orderService.getOrder(orderId, customerId));
                }

                case "getOrderList": {
                    Long customerId = getCurrentCustomerId();
                    int limit = argsNode.has("limit") ? argsNode.get("limit").asInt() : 10;
                    Pageable pageable = PageRequest.of(0, limit);
                    return objectMapper.writeValueAsString(orderService.getOrderList(pageable, customerId));
                }

                case "getReviewList": {
                    Long sellerId = argsNode.has("sellerId") && !argsNode.get("sellerId").isNull()
                            ? argsNode.get("sellerId").asLong()
                            : null;

                    String productName = argsNode.has("productName") && !argsNode.get("productName").isNull()
                            ? argsNode.get("productName").asText()
                            : null;

                    int limit = argsNode.has("limit") ? argsNode.get("limit").asInt() : 10;
                    Pageable pageable = PageRequest.of(0, limit);

                    return objectMapper.writeValueAsString(
                            reviewViewService.getReviewList(sellerId, productName, pageable));
                }

                case "getWishlistForCustomer": {
                    Long customerId = getCurrentCustomerId();
                    return objectMapper.writeValueAsString(wishlistService.getWishlistForCustomer(customerId));
                }

                case "getProductInfo": {
                    Long productId = argsNode.get("productId").asLong();
                    Long sellerId = argsNode.has("sellerId") && !argsNode.get("sellerId").isNull()
                            ? argsNode.get("sellerId").asLong()
                            : null;
                    return objectMapper.writeValueAsString(productService.getProductDetail(productId, sellerId));
                }

                case "getPublicSellerInfoByProductId": {
                    Long productId = argsNode.get("productId").asLong();
                    return objectMapper.writeValueAsString(productService.getPublicSellerInfoByProductId(productId));
                }

                case "getFeaturedSellersWithProducts": {
                    int candidateSize = argsNode.get("candidateSize").asInt();
                    int sellersPick = argsNode.get("sellersPick").asInt();
                    int productsPerSeller = argsNode.get("productsPerSeller").asInt();
                    int minReviews = argsNode.get("minReviews").asInt();
                    return objectMapper.writeValueAsString(productService.getFeaturedSellersWithProducts(candidateSize, sellersPick, productsPerSeller, minReviews));
                }

                case "searchProducts": {
                    int limit = argsNode.has("limit") ? argsNode.get("limit").asInt() : 10;
                    Long categoryId = argsNode.has("categoryId") && !argsNode.get("categoryId").isNull()
                            ? argsNode.get("categoryId").asLong()
                            : null;
                    PageRequestDTO pageRequestDTO = new PageRequestDTO();
                    pageRequestDTO.setPage(1);
                    pageRequestDTO.setSize(limit);
                    return objectMapper.writeValueAsString(productViewService.search(pageRequestDTO, categoryId));
                }

                case "getRelatedProducts": {
                    Long productId = argsNode.get("productId").asLong();
                    return objectMapper.writeValueAsString(productViewService.getRelatedProducts(productId));
                }

                case "getPopularProducts": {
                    return objectMapper.writeValueAsString(productViewService.getPopularProducts());
                }

                case "getRecommendedProductsByCategory": {
                    Long categoryId = argsNode.get("categoryId").asLong();
                    int limit = argsNode.has("limit") ? argsNode.get("limit").asInt() : 6;
                    return objectMapper.writeValueAsString(productViewService.getRecommendedProductsByCategory(categoryId, limit));
                }

                case "getActiveAuctions": {
                    int auctionLimit = argsNode.has("limit") ? argsNode.get("limit").asInt() : 10;
                    String categoryFilter = argsNode.has("categoryFilter") && !argsNode.get("categoryFilter").isNull()
                            ? argsNode.get("categoryFilter").asText()
                            : null;
                    String statusFilter = argsNode.has("statusFilter") && !argsNode.get("statusFilter").isNull()
                            ? argsNode.get("statusFilter").asText()
                            : null;
                    Pageable auctionPageable = PageRequest.of(0, auctionLimit);
                    return objectMapper.writeValueAsString(auctionService.getActiveAuctions(auctionPageable, categoryFilter, statusFilter));
                }

                case "getAuctionDetails": {
                    Integer auctionId = argsNode.get("auctionId").asInt();
                    return objectMapper.writeValueAsString(auctionService.getAuctionDetails(auctionId));
                }

                case "generateProductDescription": {
                    String productName = argsNode.get("productName").asText();

                    List<String> features = new ArrayList<>();
                    JsonNode featuresNode = argsNode.get("productFeatures");
                    if (featuresNode != null && featuresNode.isArray()) {
                        for (JsonNode featureNode : featuresNode) {
                            features.add(featureNode.asText());
                        }
                    }

                    GenerateProductDescriptionRequestDTO dto = new GenerateProductDescriptionRequestDTO();
                    dto.setProductName(productName);
                    dto.setFeatures(features);

                    return objectMapper.writeValueAsString(productService.generateDescription(dto));
                }

                default: {
                    return "{\"error\": \"알 수 없는 함수 호출: " + functionName + "\"}";
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            return "{\"error\": \"함수 호출 처리 중 예외 발생: " + e.getMessage() + "\"}";
        }
    }


    // --- 기존 메서드들 아래에 추가 ---
    private String formatChatbotResponse(String content) {
        if (content == null) return "";

        // 1. 이미지 마크다운 제거: ![텍스트](URL)
        content = content.replaceAll("!\\[[^\\]]*\\]\\([^\\)]+\\)", "");

        // 2. 연속 줄바꿈 정리 (2개 이상 -> 2개로 고정)
        content = content.replaceAll("\\n{3,}", "\n\n");

        // 3. "숫자. 제목" 패턴이 나오면 줄바꿈 삽입
        content = content.replaceAll("(\\d+)\\. ", "\n$1. ");

        // 4. "- 키: 값" 패턴을 줄바꿈
        content = content.replaceAll("\\s*- ", "\n- ");

        // 5. 앞뒤 공백 정리
        content = content.trim();

        // 6. 마크다운 제거
        content = content
                .replaceAll("\\*\\*(.*?)\\*\\*", "$1") // **굵게**
                .replaceAll("###\\s*", "")             // ### 제목 제거
                .replaceAll("##\\s*", "")              // ## 제목 제거
                .replaceAll("#\\s*", "")               // # 제목 제거
                .replaceAll("`([^`]*)`", "$1")         // `코드` 제거
                .replaceAll("!\\[[^\\]]*\\]\\([^)]*\\)", "") // 이미지 제거
                .replaceAll("\\[[^\\]]*\\]\\([^)]*\\)", "") // 링크 제거

                // 7. 리스트 포맷 정리
                .replaceAll("\\s*-\\s*", "\n- ")        // 하이픈 리스트 줄바꿈
                .replaceAll("(\\d+)\\.\\s*", "\n$1. ")  // 숫자 리스트 줄바꿈

                // 8. 불필요한 줄바꿈/공백 정리
                .replaceAll("\n{3,}", "\n\n")           // 줄바꿈 3번 이상 → 2번
                .trim();

        return content;
    }

}