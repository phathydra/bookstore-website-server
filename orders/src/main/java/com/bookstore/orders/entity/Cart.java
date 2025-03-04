package com.bookstore.orders.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Data;
import java.util.List;

@Document(collection = "carts")
@Data
public class Cart {

    @Id
    private String cartId;

    private String accountId;

    // Lưu ý: CartItem có thể là một document nhúng (embedded document)
    private List<CartItem> cartItems;
}
