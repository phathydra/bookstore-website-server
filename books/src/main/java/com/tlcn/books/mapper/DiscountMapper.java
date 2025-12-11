package com.tlcn.books.mapper;

import com.tlcn.books.dto.DiscountDto;
import com.tlcn.books.entity.Discount;

public class DiscountMapper {

    // Chuyển từ Entity (Database) -> DTO (Frontend)
    public static DiscountDto mapToDiscountDto(Discount discount, DiscountDto discountDto){
        discountDto.setId(discount.getId());
        discountDto.setPercentage(discount.getPercentage());
        discountDto.setStartDate(discount.getStartDate());
        discountDto.setEndDate(discount.getEndDate());

        // Mới thêm: Map trường type để Frontend biết đây là Flash Sale hay Normal
        discountDto.setType(discount.getType());

        return discountDto;
    }

    // Chuyển từ DTO (Frontend gửi lên) -> Entity (Lưu Database)
    public static Discount mapToDiscount(DiscountDto discountDto, Discount discount){
        discount.setPercentage(discountDto.getPercentage());
        discount.setStartDate(discountDto.getStartDate());
        discount.setEndDate(discountDto.getEndDate());

        // Mới thêm: Map trường type để lưu đúng loại giảm giá
        discount.setType(discountDto.getType());

        return discount;
    }
}