package com.realive.controller.chatbot;

import com.realive.dto.chatbot.ChatbotRequestDTO;
import com.realive.dto.chatbot.ChatbotResponseDTO;
import com.realive.service.chatbot.ChatbotService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatbotController {

    private final ChatbotService chatbotService;

    @PostMapping
    public ResponseEntity<ChatbotResponseDTO> getChatReply(@RequestBody ChatbotRequestDTO request) {
        // GPT에게 사용자 메시지를 보내고 응답 받기
        String reply = chatbotService.getChatbotReply(request.getMessage());

        // 응답 DTO로 감싸서 반환
        ChatbotResponseDTO responseDTO = new ChatbotResponseDTO(reply);
        return ResponseEntity.ok(responseDTO);
    }
}