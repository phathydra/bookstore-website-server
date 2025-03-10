package com.bookstore.orders.service.impl;

import com.bookstore.orders.dto.CartDto;
import com.bookstore.orders.dto.CartItemDto;
import com.bookstore.orders.entity.Cart;
import com.bookstore.orders.entity.CartItem;
import com.bookstore.orders.mapper.CartMapper;
import com.bookstore.orders.repository.CartRepository;
import com.bookstore.orders.service.ICartService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CartServiceImpl implements ICartService {

    private final CartRepository cartRepository;
    private final CartMapper cartMapper;

    public CartServiceImpl(CartRepository cartRepository, CartMapper cartMapper) {
        this.cartRepository = cartRepository;
        this.cartMapper = cartMapper;
    }

    @Override
    public ResponseEntity<?> createCart(String accountId) {
        Optional<Cart> cartOptional = cartRepository.findByAccountId(accountId);

        if (cartOptional.isEmpty()) {
            Cart cart = new Cart();
            cart.setAccountId(accountId);
            cart.setCartItems(new ArrayList<>());
            cartRepository.save(cart);
            return ResponseEntity.ok("Giỏ hàng mặc định đã được tạo.");
        }

        return ResponseEntity.ok("Giỏ hàng đã tồn tại.");
    }

    @Override
    public ResponseEntity<?> getCartByAccountId(String accountId) {
        Optional<Cart> cartOptional = cartRepository.findByAccountId(accountId);

        if (cartOptional.isPresent()) {
            return ResponseEntity.ok(cartMapper.toDto(cartOptional.get()));
        }
        return ResponseEntity.badRequest().body("Không tìm thấy giỏ hàng!");
    }

    @Override
    public ResponseEntity<?> addToCart(CartDto cartDto) {
        Optional<Cart> cartOptional = cartRepository.findByAccountId(cartDto.getAccountId());
        Cart cart = cartOptional.orElseGet(() -> {
            Cart newCart = new Cart();
            newCart.setAccountId(cartDto.getAccountId());
            newCart.setCartItems(new ArrayList<>());
            return cartRepository.save(newCart);
        });

        for (CartItemDto cartItemDto : cartDto.getCartItems()) {
            Optional<CartItem> existingItem = cart.getCartItems().stream()
                    .filter(item -> item.getBookId().equals(cartItemDto.getBookId()))
                    .findFirst();

            if (existingItem.isPresent()) {
                CartItem cartItem = existingItem.get();
                cartItem.setQuantity(cartItem.getQuantity() + cartItemDto.getQuantity());
            } else {
                cart.getCartItems().add(cartMapper.toCartItemEntity(cartItemDto));
            }
        }

        cartRepository.save(cart);
        return ResponseEntity.ok("Giỏ hàng đã được cập nhật!");
    }

    @Override
    public ResponseEntity<?> updateQuantity(String accountId, String bookId, int quantity) {
        Optional<Cart> cartOptional = cartRepository.findByAccountId(accountId);

        if (cartOptional.isPresent()) {
            Cart cart = cartOptional.get();
            Optional<CartItem> cartItemOptional = cart.getCartItems().stream()
                    .filter(item -> item.getBookId().equals(bookId))
                    .findFirst();

            if (cartItemOptional.isPresent()) {
                CartItem cartItem = cartItemOptional.get();
                if (quantity > 0) {
                    cartItem.setQuantity(quantity);
                } else {
                    cart.getCartItems().remove(cartItem);
                }
                cartRepository.save(cart);
                return ResponseEntity.ok("Số lượng sản phẩm đã được cập nhật!");
            }
        }
        return ResponseEntity.badRequest().body("Không tìm thấy giỏ hàng hoặc sản phẩm!");
    }

    @Override
    public ResponseEntity<?> removeItem(String accountId, String bookId) {
        Optional<Cart> cartOptional = cartRepository.findByAccountId(accountId);

        if (cartOptional.isPresent()) {
            Cart cart = cartOptional.get();
            cart.getCartItems().removeIf(item -> item.getBookId().equals(bookId));
            cartRepository.save(cart);
            return ResponseEntity.ok("Sản phẩm đã được xóa khỏi giỏ hàng!");
        }
        return ResponseEntity.badRequest().body("Không tìm thấy giỏ hàng!");
    }
}
