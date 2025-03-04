package com.bookstore.orders.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class CartItemDto {
    @NotEmpty(message = "ID sách không được để trống")
    private String bookId;

    @Min(value = 1, message = "Số lượng phải lớn hơn hoặc bằng 1")
    private int quantity;
}
