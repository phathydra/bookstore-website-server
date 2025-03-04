package com.bookstore.orders.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;

@Data
public class OrderItem {

    @Id
    private String id; // Trường này có thể được sinh tự động nếu cần

    private String bookId;

    private String bookName;

    private int quantity;

    private double price;
}
