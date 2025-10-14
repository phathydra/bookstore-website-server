package com.bookstore.chatbot.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "auto_message")
public class AutoMessage {

    @Id
    private String id;

    private String accountId;

    private String type;

    private String content;

    private String metadata;
}
