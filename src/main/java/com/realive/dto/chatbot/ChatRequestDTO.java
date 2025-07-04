package com.realive.dto.chatbot;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatRequestDTO {
    private String model;
    private List<Message> messages;

    // Function Calling 관련 필드
    private List<FunctionDefinition> functions;
    private Object function_call; // "auto" 또는 { name: "함수명" }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Message {
        private String role;
        private String content;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class FunctionDefinition {
        private String name;
        private String description;
        private Map<String, Object> parameters;
    }

    // 기존 단순 메시지 생성
    public static ChatRequestDTO of(String model, String userMessage) {
        return new ChatRequestDTO(model,
                List.of(new Message("user", userMessage)),
                null,   // functions 없음
                "auto"  // function_call 기본값
        );
    }

    // 함수 리스트까지 포함하는 버전
    public static ChatRequestDTO withFunctions(String model, String userMessage, List<FunctionDefinition> functions) {
        return new ChatRequestDTO(model,
                List.of(new Message("user", userMessage)),
                functions,
                "auto"
        );
    }
}
