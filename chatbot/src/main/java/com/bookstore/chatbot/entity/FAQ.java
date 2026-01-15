package com.bookstore.chatbot.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "faq")
public class FAQ {

    @Id
    private String id;

    private String question;
    private String answer;
}
