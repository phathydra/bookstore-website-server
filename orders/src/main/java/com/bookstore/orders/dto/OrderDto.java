package com.bookstore.orders.dto;

import lombok.Data;
import java.util.List;

@Data
public class OrderDto {
    private String accountId;
    private String phoneNumber;
    private String recipientName;
    private String country;
    private String city;
    private String district;
    private String ward;
    private String note;
    private double totalPrice;
    private List<OrderItemDto> orderItems;
    private String orderStatus;
    private String shippingStatus;
}
