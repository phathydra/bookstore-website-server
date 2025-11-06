package com.bookstore.orders.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DiscountApplicationDto {
    private String discountName; // Tên của khuyến mãi (vd: "Combo Dọn Kho")
    private Double amount; // Số tiền được giảm
}