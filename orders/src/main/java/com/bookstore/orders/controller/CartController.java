package com.bookstore.orders.controller;

import com.bookstore.orders.dto.CartDto;
import com.bookstore.orders.dto.CartItemDto;
import com.bookstore.orders.entity.Cart;
import com.bookstore.orders.entity.CartItem;
import com.bookstore.orders.repository.CartRepository;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/cart")
public class CartController {

    private final CartRepository cartRepository;

    public CartController(CartRepository cartRepository) {
        this.cartRepository = cartRepository;
    }

    @PostMapping("/add")
    public ResponseEntity<?> addToCart(@Valid @RequestBody CartDto cartDto) {
        // Tìm giỏ hàng theo accountId
        Optional<Cart> cartOptional = cartRepository.findByAccountId(cartDto.getAccountId());

        Cart cart;
        if (cartOptional.isPresent()) {
            cart = cartOptional.get();
        } else {
            // Tạo giỏ hàng mới nếu chưa có
            cart = new Cart();
            cart.setAccountId(cartDto.getAccountId());
            cart.setCartItems(new ArrayList<>());
            cart = cartRepository.save(cart);
        }

        // Xóa danh sách cũ (nếu muốn cập nhật lại toàn bộ)
        cart.getCartItems().clear();

        // Để sử dụng trong lambda, gán cart cho biến final
        final Cart finalCart = cart;
        List<CartItem> cartItems = cartDto.getCartItems().stream()
                .map(cartItemDto -> {
                    CartItem cartItem = new CartItem();
                    cartItem.setBookId(cartItemDto.getBookId());
                    cartItem.setQuantity(cartItemDto.getQuantity());
                    return cartItem;
                })
                .collect(Collectors.toList());

        finalCart.getCartItems().addAll(cartItems);
        cartRepository.save(finalCart);

        return ResponseEntity.ok("Giỏ hàng đã được cập nhật!");
    }
}
