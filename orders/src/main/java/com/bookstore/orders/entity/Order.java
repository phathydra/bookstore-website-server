package com.bookstore.orders.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.List;

@Document(collection = "orders")
@Data
public class Order {

    @Id
    private String orderId;

    private String accountId;

    private String customerName;

    private String email;

    private String phone;

    private String address;

    private String paymentMethod; // VD: "COD", "Momo", "Bank"

    private double totalPrice;

    private List<OrderItem> orderItems;
}
