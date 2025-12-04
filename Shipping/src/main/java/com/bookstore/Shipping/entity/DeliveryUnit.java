package com.bookstore.Shipping.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "deliveryUnits")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeliveryUnit {
    @Id
    private String id; // MongoDB _id

    private String deliveryUnitId; // field riêng để map với accountId
    private String name;
    private String email;
    private String phone;
    private String branchAddress;
    private String unit;
}
