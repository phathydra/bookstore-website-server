package com.bookstore.orders.dto;

import lombok.Data;
import java.util.List;

@Data
public class CartItemResponseDto {
    private String bookId;
    private String bookName;
    private List<String> bookImages;
    private Double originalPrice; // Giá gốc mới nhất từ `books` service
    private int quantity;
    private Double lineItemTotal; // originalPrice * quantity
}