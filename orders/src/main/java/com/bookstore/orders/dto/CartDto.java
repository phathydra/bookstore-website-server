package com.bookstore.orders.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor  // Tạo constructor không tham số (cần thiết cho Spring)
@AllArgsConstructor // Tạo constructor có tham số
public class CartDto {
    private String cartId;

    @NotEmpty(message = "ID tài khoản không được để trống")
    private String accountId;

    @NotEmpty(message = "Giỏ hàng không được để trống")
    private List<CartItemDto> cartItems;

    // Thêm constructor 2 tham số (nếu không cần cartId)
    public CartDto(String accountId, List<CartItemDto> cartItems) {
        this.accountId = accountId;
        this.cartItems = cartItems;
    }
}
