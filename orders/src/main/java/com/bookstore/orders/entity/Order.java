package com.bookstore.orders.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.List;
import java.time.LocalDateTime;  // Import LocalDateTime

@Data
@Document(collection = "orders")
public class Order {
    @Id
    private String orderId;
    private String accountId;
    private String phoneNumber;
    private String recipientName;
    private String country;
    private String city;
    private String district;
    private String ward;
    private String note;
    private double totalPrice;
    private List<OrderItem> orderItems;
    private String orderStatus;
    private String shippingStatus;
    private String paymentMethod;
    private LocalDateTime dateOrder;  // Add this field for order date
}
