package com.bookstore.chatbot.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ConversationDto {
    private String id;

    private String title1;

    private String title2;

    private String accountId1;

    private String accountId2;

    private String channelType;

    private LocalDateTime createdAt;

    private LocalDateTime lastUpdated;
}
