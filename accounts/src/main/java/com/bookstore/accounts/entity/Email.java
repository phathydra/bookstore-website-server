package com.bookstore.accounts.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "emails")
@Getter @Setter @ToString @AllArgsConstructor @NoArgsConstructor
public class Email {
    @Id
    private String id;
    private String to;         // Email người nhận
    private String subject;    // Tiêu đề
    private String content;    // Nội dung
    private String status;     // Trạng thái gửi (Success/Failed)
}
