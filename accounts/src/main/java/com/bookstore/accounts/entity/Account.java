package com.bookstore.accounts.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "accounts")
@Getter @Setter @ToString @AllArgsConstructor @NoArgsConstructor
public class Account{

    @Id
    private String accountId;

    private String username;

    private String password;

    private String role;
}
