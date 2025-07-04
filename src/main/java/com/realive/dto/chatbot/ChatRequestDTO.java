package com.realive.dto.chatbot;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ChatRequestDTO {
    private String model;
    private List<Message> messages;

    @Data
    @AllArgsConstructor
    public static class Message {
        private String role;
        private String content;
    }

    public static ChatRequestDTO of(String model, String userMessage) {
        return new ChatRequestDTO(model, List.of(new Message("user", userMessage)));
    }
}
