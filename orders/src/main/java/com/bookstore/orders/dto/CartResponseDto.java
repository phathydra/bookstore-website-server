package com.bookstore.orders.dto;

import lombok.Data;
import java.util.List;

@Data
public class CartResponseDto {
    private String cartId;
    private String accountId;
    private List<CartItemResponseDto> items; // Chi tiết các sách trong giỏ
    private Double subtotal; // Tạm tính (trước khi giảm giá)
    private List<DiscountApplicationDto> appliedDiscounts; // Các combo/giảm giá đã áp dụng
    private Double totalDiscountAmount; // Tổng số tiền giảm
    private Double total; // Giá cuối cùng
}