package com.bookstore.orders.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class OrderDto {

    private String orderId;

    @NotEmpty(message = "ID tài khoản không được để trống")
    private String accountId;

    @NotEmpty(message = "Tên khách hàng không được để trống")
    private String customerName;

    @NotEmpty(message = "Email không được để trống")
    private String email;

    @NotEmpty(message = "Số điện thoại không được để trống")
    private String phone;

    @NotEmpty(message = "Địa chỉ không được để trống")
    private String address;

    @NotEmpty(message = "Phương thức thanh toán không được để trống")
    private String paymentMethod; // VD: "COD", "Momo", "Bank"

    @Min(value = 0, message = "Tổng tiền phải lớn hơn hoặc bằng 0")
    private double totalPrice;

    @NotEmpty(message = "Danh sách sản phẩm không được để trống")
    private List<OrderItemDto> orderItems;
}

@Data
class OrderItemDto {

    @NotEmpty(message = "ID sách không được để trống")
    private String bookId;

    @NotEmpty(message = "Tên sách không được để trống")
    private String bookName;

    @Min(value = 1, message = "Số lượng phải lớn hơn hoặc bằng 1")
    private int quantity;

    @Min(value = 0, message = "Giá không hợp lệ")
    private double price;
}
