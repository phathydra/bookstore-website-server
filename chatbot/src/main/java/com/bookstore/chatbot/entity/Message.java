package com.bookstore.chatbot.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "message")
public class Message {

    @Id
    private String id;

    private String conversationId;

    private String content;

    private String sender;

    private LocalDateTime createdAt;
}
