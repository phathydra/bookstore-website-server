package com.bookstore.Shipping.dto;

import lombok.Data;

@Data
public class ShippingDto {
    private String email;
    private String password;
    private String role; // Thêm trường role
}