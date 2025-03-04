package com.bookstore.orders.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import java.util.List;

@Data
public class CartDto {
    private String cartId;

    @NotEmpty(message = "ID tài khoản không được để trống")
    private String accountId;

    @NotEmpty(message = "Giỏ hàng không được để trống")
    private List<CartItemDto> cartItems;
}
