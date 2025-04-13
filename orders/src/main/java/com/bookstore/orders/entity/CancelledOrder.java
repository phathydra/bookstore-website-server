package com.bookstore.orders.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Data
@Document(collection = "cancelled_orders")
public class CancelledOrder {
    @Id
    private String id; // Có thể đặt tên khác nếu muốn, ví dụ: cancelledOrderId
    private String orderId;
    private String cancellationReason;
    private String cancellationStatus; // Ví dụ: "Pending", "Approved", "Rejected"
    private LocalDateTime cancellationDate;
}