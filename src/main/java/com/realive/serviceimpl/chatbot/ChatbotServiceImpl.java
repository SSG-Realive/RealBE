package com.realive.serviceimpl.chatbot;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.realive.dto.chatbot.ChatApiResponseDTO;
import com.realive.dto.chatbot.ChatRequestDTO;
import com.realive.service.chatbot.ChatbotService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class ChatbotServiceImpl implements ChatbotService {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    @Value("${openai.api.key}")
    private String openAiApiKey;

    @Value("${openai.model:gpt-3.5-turbo}")
    private String model;

    @Override
    public String getChatbotReply(String message) {
        // DTO로 요청 구성
        ChatRequestDTO request = ChatRequestDTO.of(model, message);

        try {
            // OpenAI에 POST 요청
            String responseJson = webClient.post()
                    .uri("https://api.openai.com/v1/chat/completions")
                    .header("Authorization", "Bearer " + openAiApiKey)
                    .header("Content-Type", "application/json")
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(String.class)
                    .onErrorResume(e -> {
                        e.printStackTrace();
                        return Mono.just("{\"error\": \"GPT 호출 실패\"}");
                    })
                    .block();

            // JSON 파싱 → content 추출
            ChatApiResponseDTO response = objectMapper.readValue(responseJson, ChatApiResponseDTO.class);
            return response.getChoices().get(0).getMessage().getContent();

        } catch (Exception e) {
            e.printStackTrace();
            return "⚠️ 챗봇 응답 처리 중 오류 발생";
        }
    }
}
