package com.bookstore.orders.entity;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Document(collection = "combos")
@Data
public class Combo {
    @Id
    private String comboId;

    @NotEmpty
    private String name; // Tên combo, vd: "Combo Dọn Kho Mùa Hè"
    private String image;
    private String description;

    @NotEmpty
    private List<String> bookIds; // Danh sách các bookId trong combo

    @NotNull
    private DiscountType discountType; // Loại giảm giá

    @NotNull
    private Double discountValue; // Giá trị giảm

    private boolean isActive = true;
    private LocalDateTime startDate;
    private LocalDateTime endDate;

    public enum DiscountType {
        PERCENT,
        FIXED_AMOUNT
    }
}