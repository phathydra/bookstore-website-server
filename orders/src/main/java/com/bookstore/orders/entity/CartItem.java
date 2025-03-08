package com.bookstore.orders.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "cart_items")
@Data
public class CartItem {

    @Id
    private String id;
    private String cartId;
    private String bookId;
    private String bookName;
    private String bookImage; // Thêm ảnh sách
    private int quantity;
    private double price;
}
