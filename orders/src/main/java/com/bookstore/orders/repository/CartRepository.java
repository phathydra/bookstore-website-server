package com.bookstore.orders.repository;

import com.bookstore.orders.entity.Cart;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface CartRepository extends MongoRepository<Cart, String> {
    Optional<Cart> findByAccountId(String accountId);
}
