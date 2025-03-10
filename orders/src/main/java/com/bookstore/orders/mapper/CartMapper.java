package com.bookstore.orders.mapper;

import com.bookstore.orders.dto.CartDto;
import com.bookstore.orders.dto.CartItemDto;
import com.bookstore.orders.entity.Cart;
import com.bookstore.orders.entity.CartItem;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class CartMapper {

    public CartDto toDto(Cart cart) {
        List<CartItemDto> cartItemDtos = cart.getCartItems().stream()
                .map(this::toCartItemDto)
                .collect(Collectors.toList());

        return new CartDto(cart.getCartId(), cart.getAccountId(), cartItemDtos);
    }

    public Cart toEntity(CartDto cartDto) {
        List<CartItem> cartItems = cartDto.getCartItems().stream()
                .map(this::toCartItemEntity)
                .collect(Collectors.toList());

        Cart cart = new Cart();
        cart.setCartId(cartDto.getCartId());
        cart.setAccountId(cartDto.getAccountId());
        cart.setCartItems(cartItems);
        return cart;
    }

    public CartItemDto toCartItemDto(CartItem cartItem) {
        return new CartItemDto(
                cartItem.getBookId(),
                cartItem.getBookName(),
                cartItem.getBookImage(),
                cartItem.getQuantity(),
                cartItem.getPrice()
        );
    }

    public CartItem toCartItemEntity(CartItemDto cartItemDto) {
        CartItem cartItem = new CartItem();
        cartItem.setBookId(cartItemDto.getBookId());
        cartItem.setBookName(cartItemDto.getBookName());
        cartItem.setBookImage(cartItemDto.getBookImage());
        cartItem.setQuantity(cartItemDto.getQuantity());
        cartItem.setPrice(cartItemDto.getPrice());
        return cartItem;
    }
}
