package com.bookstore.orders.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "order_items")
public class OrderItem {
    @Id
    private String id;
    private String bookId;
    private String bookName;
    private String bookImage;
    private int quantity;
    private double price;
}
