package com.bookstore.Shipping.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "accounts")
@Data // sinh getter, setter, toString, equals, hashCode
@AllArgsConstructor
@NoArgsConstructor
public class Shipping {
    @Id
    private String accountId;

    private String email;
    private String password;
    private String role;   // "Shipper"
    private String status; // "Active"
}
