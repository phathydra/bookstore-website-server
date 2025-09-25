package com.bookstore.chatbot.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ConversationDto {
    private String id;

    private String userId;

    private LocalDateTime createdAt;

    private LocalDateTime lastUpdated;
}
