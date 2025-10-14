package com.bookstore.chatbot.dto;

import lombok.Data;

@Data
public class AutoMessageDto {

    private String id;

    private String accountId;

    private String type;

    private String content;

    private String metadata;
}
