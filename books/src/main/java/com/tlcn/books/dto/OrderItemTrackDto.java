package com.tlcn.books.dto; // (Phải cùng package với file kia)

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class OrderItemTrackDto {
    private String bookId;
    private int quantity;
    private Double price; // Đảm bảo khớp với kiểu dữ liệu bên React
}