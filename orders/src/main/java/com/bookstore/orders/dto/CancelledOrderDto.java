package com.bookstore.orders.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CancelledOrderDto {
    private String orderId;
    private String cancellationReason;
    private String cancellationStatus;
    private LocalDateTime cancellationDate; // Optional: Có thể thêm nếu muốn trả về thời gian hủy
}