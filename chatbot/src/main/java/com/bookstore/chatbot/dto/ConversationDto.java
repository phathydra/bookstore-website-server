package com.bookstore.chatbot.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ConversationDto {
    private String id;

    private String title;

    private String userId1;

    private String userId2;

    private String channelType;

    private LocalDateTime createdAt;

    private LocalDateTime lastUpdated;
}
