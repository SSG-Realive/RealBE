package com.realive.serviceimpl.chatbot;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.realive.dto.chatbot.ChatApiResponseDTO;
import com.realive.dto.chatbot.ChatRequestDTO;
import com.realive.security.customer.CustomerPrincipal;
import com.realive.service.chatbot.ChatbotService;
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

    @Value("${openai.api.key}")
    private String openAiApiKey;

    @Value("${openai.model}")
    private String model;

    private Long getCurrentCustomerId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new RuntimeException("로그인한 사용자만 사용할 수 있는 기능입니다.");
        }

        // CustomUserDetails 또는 JwtUserDetails 등 구현에 따라 타입 캐스팅
        CustomerPrincipal userDetails = (CustomerPrincipal) auth.getPrincipal();
        return userDetails.getId(); // 또는 getCustomerId()
    }

    public ChatbotServiceImpl(@Qualifier("openAiWebClient") WebClient webClient,
                              ObjectMapper objectMapper,
                              ProductService productService,
                              OrderService orderService,
                              ReviewViewService reviewViewService,
                              WishlistService wishlistService) {
        this.webClient = webClient;
        this.objectMapper = objectMapper;
        this.productService = productService;
        this.orderService = orderService;
        this.reviewViewService = reviewViewService;
        this.wishlistService = wishlistService;
        log.info("[ChatbotServiceImpl] WebClient 빈 확인: {}", webClient);
    }

    @Override
    public String getChatbotReply(String message) {
        // 1. GPT에게 함수 호출 가능한 메시지 보냄 (functions 포함)
        ChatRequestDTO request = ChatRequestDTO.withFunctions(
                model,
                message,
                FunctionSchemaFactory.getAllFunctions()
        );

        try {
            // 요청 JSON 문자열로 변환 후 로그 출력
            String requestJson = objectMapper.writeValueAsString(request);
            log.info("OpenAI 요청 JSON: {}", requestJson);

            // OpenAI API 호출 - 문자열(JSON)로 body 전달
            String responseJson = webClient.post()
                    .uri("https://api.openai.com/v1/chat/completions")
                    .header("Authorization", "Bearer " + openAiApiKey)
                    .header("Content-Type", "application/json")
                    .bodyValue(requestJson)  // 반드시 문자열로 보낼 것
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            // JSON 파싱
            JsonNode root = objectMapper.readTree(responseJson);
            JsonNode choice = root.path("choices").get(0);
            JsonNode messageNode = choice.path("message");

            // 2. function_call 존재하면 함수 호출 처리
            if (messageNode.has("function_call")) {
                String functionName = messageNode.get("function_call").get("name").asText();
                String argumentsJson = messageNode.get("function_call").get("arguments").asText();

                // 3. 함수 호출 결과 받기
                String functionResult = handleFunctionCall(functionName, argumentsJson);

                // 4. 함수 호출 결과를 GPT에게 다시 보내서 답변 생성
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
                // 함수 호출 없는 일반 답변
                ChatApiResponseDTO response = objectMapper.readValue(responseJson, ChatApiResponseDTO.class);
                return response.getChoices().get(0).getMessage().getContent();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "⚠️ 챗봇 응답 처리 중 오류 발생";
        }
    }

    private String handleFunctionCall(String functionName, String argumentsJson) {
        try {
            JsonNode argsNode = objectMapper.readTree(argumentsJson);

            switch (functionName) {
                case "getOrderDetail": {
                    Long orderId = argsNode.get("orderId").asLong();
                    Long customerId = getCurrentCustomerId();
                    var orderDetail = orderService.getOrder(orderId, customerId);
                    return objectMapper.writeValueAsString(orderDetail);
                }

                case "getOrderList": {
                    Long customerId = getCurrentCustomerId();
                    int limit = argsNode.has("limit") ? argsNode.get("limit").asInt() : 10;
                    Pageable pageable = PageRequest.of(0, limit);
                    var orderList = orderService.getOrderList(pageable, customerId);
                    return objectMapper.writeValueAsString(orderList);
                }

                case "getReviewList": {
                    Long customerId = getCurrentCustomerId();
                    int limit = argsNode.has("limit") ? argsNode.get("limit").asInt() : 10;
                    Pageable pageable = PageRequest.of(0, limit);
                    var reviews = reviewViewService.getMyReviewList(customerId, pageable); // ← 메서드가 있다면
                    return objectMapper.writeValueAsString(reviews);
                }

                case "getWishlistForCustomer": {
                    Long customerId = getCurrentCustomerId();
                    var wishlist = wishlistService.getWishlistForCustomer(customerId); // ← 메서드가 있다면
                    return objectMapper.writeValueAsString(wishlist);
                }

                case "getProductInfo": {
                    Long productId = argsNode.get("productId").asLong();
                    Long sellerId = argsNode.has("sellerId") && !argsNode.get("sellerId").isNull()
                            ? argsNode.get("sellerId").asLong()
                            : null;
                    var productInfo = productService.getProductDetail(productId, sellerId);
                    return objectMapper.writeValueAsString(productInfo);
                }

                case "getPublicSellerInfoByProductId": {
                    Long productId = argsNode.get("productId").asLong();
                    var sellerInfo = productService.getPublicSellerInfoByProductId(productId); // ← 메서드가 있다면
                    return objectMapper.writeValueAsString(sellerInfo);
                }

                case "getFeaturedSellersWithProducts": {
                    int candidateSize = argsNode.get("candidateSize").asInt();
                    int sellersPick = argsNode.get("sellersPick").asInt();
                    int productsPerSeller = argsNode.get("productsPerSeller").asInt();
                    int minReviews = argsNode.get("minReviews").asInt();

                    var featured = productService.getFeaturedSellersWithProducts(candidateSize, sellersPick, productsPerSeller, minReviews);
                    return objectMapper.writeValueAsString(featured);
                }

                default:
                    return "{\"error\": \"알 수 없는 함수 호출: " + functionName + "\"}";
            }

        } catch (Exception e) {
            e.printStackTrace();
            return "{\"error\": \"함수 호출 처리 중 예외 발생: " + e.getMessage() + "\"}";
        }
    }
}

