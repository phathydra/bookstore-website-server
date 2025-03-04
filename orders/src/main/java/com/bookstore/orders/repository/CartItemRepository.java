package com.bookstore.orders.repository;

import com.bookstore.orders.entity.CartItem;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface CartItemRepository extends MongoRepository<CartItem, String> {
    Optional<CartItem> findByCartIdAndBookId(String cartId, String bookId);
}
