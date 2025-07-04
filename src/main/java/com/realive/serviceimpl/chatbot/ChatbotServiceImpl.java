package com.realive.serviceimpl.chatbot;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.realive.dto.chatbot.ChatApiResponseDTO;
import com.realive.dto.chatbot.ChatRequestDTO;
import com.realive.service.chatbot.ChatbotService;
import com.realive.service.order.OrderService;
import com.realive.service.order.OrderServiceImpl;
import com.realive.service.product.ProductService;
import com.realive.serviceimpl.product.ProductServiceImpl;
import com.realive.util.FunctionSchemaFactory;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@Log4j2
public class ChatbotServiceImpl implements ChatbotService {

    @Qualifier("openAiWebClient")
    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    private final ProductService productService;
    private final OrderService orderService;

    @Value("${openai.api.key}")
    private String openAiApiKey;

    @Value("${openai.model:gpt-3.5-turbo}")
    private String model;

    public ChatbotServiceImpl(@Qualifier("openAiWebClient") WebClient webClient,
                              ObjectMapper objectMapper,
                              ProductService productService,
                              OrderService orderService) {
        this.webClient = webClient;
        this.objectMapper = objectMapper;
        this.productService = productService;
        this.orderService = orderService;
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
            // OpenAI API 호출
            String responseJson = webClient.post()
                    .uri("https://api.openai.com/v1/chat/completions")
                    .header("Authorization", "Bearer " + openAiApiKey)
                    .header("Content-Type", "application/json")
                    .bodyValue(request)
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
                        new ChatRequestDTO.Message("function", functionResult)
                ));

                String followupResponseJson = webClient.post()
                        .uri("https://api.openai.com/v1/chat/completions")
                        .header("Authorization", "Bearer " + openAiApiKey)
                        .header("Content-Type", "application/json")
                        .bodyValue(followupRequest)
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
            switch (functionName) {
                case "getOrderDetail":
                    JsonNode argsNode = objectMapper.readTree(argumentsJson);
                    Long orderId = argsNode.get("orderId").asLong();
                    Long customerId = argsNode.get("customerId").asLong();

                    // 주문 상세 조회 (예시) - 실제 서비스 메서드 호출
                    var orderDetail = orderService.getOrder(orderId, customerId);

                    // JSON 문자열로 변환하여 반환 (GPT가 이해하도록 텍스트로 변환 가능)
                    return objectMapper.writeValueAsString(orderDetail);

                case "getProductInfo":
                    JsonNode prodArgs = objectMapper.readTree(argumentsJson);
                    Long productId = prodArgs.get("productId").asLong();
                    Long sellerId = prodArgs.has("sellerId") ? prodArgs.get("sellerId").asLong() : null;

                    var productInfo = productService.getProductDetail(productId, sellerId);

                    return objectMapper.writeValueAsString(productInfo);

                // 필요에 따라 다른 함수도 추가

                default:
                    return "{\"error\": \"알 수 없는 함수 호출: " + functionName + "\"}";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "{\"error\": \"함수 호출 처리 중 예외 발생\"}";
        }
    }
}
