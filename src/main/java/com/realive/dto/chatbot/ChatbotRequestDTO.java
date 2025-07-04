package com.realive.dto.chatbot;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// 현재는 축약형으로 만듬 필요시 확장
@Getter
@Setter
@NoArgsConstructor
public class ChatbotRequestDTO {
    private String message;
}