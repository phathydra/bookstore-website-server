package com.bookstore.orders.dto;

import com.bookstore.orders.entity.Combo; // Cần import enum từ entity
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

@Data
public class ComboDto {

    private String comboId; // Dùng cho việc cập nhật sau này

    @NotEmpty(message = "Tên combo không được để trống")
    private String name;

    private String description;

    @NotEmpty(message = "Combo phải có ít nhất 1 sách")
    private List<String> bookIds; // Danh sách các bookId

    @NotNull(message = "Loại giảm giá không được để trống")
    private Combo.DiscountType discountType; // Dùng enum từ Entity: PERCENT hoặc FIXED_AMOUNT

    @NotNull(message = "Giá trị giảm giá không được để trống")
    private Double discountValue;

    private boolean isActive = true;

    // Frontend gửi string ISO, nên backend nhận là String
    private String startDate;

    private String endDate;
}