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
@Document(collection = "conversation")
public class Conversation {

    @Id
    private String id;

    private String title1;

    private String title2;

    private String accountId1;

    private String accountId2;

    private String channelType;

    private LocalDateTime createdAt;

    private LocalDateTime lastUpdated;
}
