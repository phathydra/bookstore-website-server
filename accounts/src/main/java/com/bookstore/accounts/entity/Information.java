package com.bookstore.accounts.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "information")
@Getter @Setter @ToString @AllArgsConstructor @NoArgsConstructor
public class Information{

    private String accountId;

    @Id
    private String id;

    private String name;

    private String email;

    private String phone;

    private String address;

    private String avatar;
}
