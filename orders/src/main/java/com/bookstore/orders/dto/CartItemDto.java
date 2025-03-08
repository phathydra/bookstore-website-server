package com.bookstore.orders.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItemDto {
    @NotEmpty(message = "ID sách không được để trống")
    private String bookId;

    @NotEmpty(message = "Tên sách không được để trống")
    private String bookName;

    @NotEmpty(message = "Hình ảnh sách không được để trống")
    private String bookImage; // Thêm ảnh sách

    @Min(value = 1, message = "Số lượng phải lớn hơn hoặc bằng 1")
    private int quantity;

    @Min(value = 0, message = "Giá sách không được nhỏ hơn 0")
    private double price;
}
