package com.bookstore.orders.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "cart_items")
@Data
public class CartItem {

    @Id
    private String id;

    // Thêm trường cartId để liên kết đến giỏ hàng
    private String cartId;

    private String bookId;

    private int quantity;
}
