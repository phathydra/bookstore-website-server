package com.bookstore.orders.dto;

import lombok.Data;

@Data
public class OrderVoucherDto {
    private String orderId;
    private String voucherCode;

    private Double discountedPrice;
}
