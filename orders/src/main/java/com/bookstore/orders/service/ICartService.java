package com.bookstore.orders.service;

import com.bookstore.orders.dto.CartDto;
import org.springframework.http.ResponseEntity;

public interface ICartService {
    ResponseEntity<?> createCart(String accountId);
    ResponseEntity<?> getCartByAccountId(String accountId);
    ResponseEntity<?> addToCart(CartDto cartDto);
    ResponseEntity<?> updateQuantity(String accountId, String bookId, int quantity);
    ResponseEntity<?> removeItem(String accountId, String bookId);
}
