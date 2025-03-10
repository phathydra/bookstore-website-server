package com.bookstore.orders.dto;

import lombok.Data;

@Data
public class OrderItemDto {
    private String bookId;
    private String bookName;
    private String bookImage;
    private int quantity;
    private double price;
}
