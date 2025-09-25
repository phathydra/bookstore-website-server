package com.bookstore.chatbot.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MessageDto {
    private String conversationId;

    private String content;

    private String sender;

    private LocalDateTime createdAt;
}
