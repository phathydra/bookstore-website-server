package com.bookstore.chatbot.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.lang.annotation.Documented;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "conversation")
public class Conversation {

    @Id
    private String id;

    private String userId;

    private LocalDateTime createdAt;

    private LocalDateTime lastUpdated;
}
