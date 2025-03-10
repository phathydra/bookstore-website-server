package com.bookstore.orders.controller;

import com.bookstore.orders.dto.CartDto;
import com.bookstore.orders.service.ICartService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cart")
@CrossOrigin(origins = "http://localhost:3001")
public class CartController {

    private final ICartService cartService;

    public CartController(ICartService cartService) {
        this.cartService = cartService;
    }

    @PostMapping("/create/{accountId}")
    public ResponseEntity<?> createCart(@PathVariable String accountId) {
        return cartService.createCart(accountId);
    }

    @GetMapping("/{accountId}")
    public ResponseEntity<?> getCartByAccountId(@PathVariable String accountId) {
        return cartService.getCartByAccountId(accountId);
    }

    @PostMapping("/add")
    public ResponseEntity<?> addToCart(@Valid @RequestBody CartDto cartDto) {
        return cartService.addToCart(cartDto);
    }

    @PutMapping("/update/{accountId}/{bookId}")
    public ResponseEntity<?> updateQuantity(@PathVariable String accountId, @PathVariable String bookId, @RequestParam int quantity) {
        return cartService.updateQuantity(accountId, bookId, quantity);
    }

    @DeleteMapping("/remove/{accountId}/{bookId}")
    public ResponseEntity<?> removeItem(@PathVariable String accountId, @PathVariable String bookId) {
        return cartService.removeItem(accountId, bookId);
    }
}
