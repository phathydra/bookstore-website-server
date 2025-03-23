package com.tlcn.books.mapper;

import com.tlcn.books.dto.DiscountDto;
import com.tlcn.books.entity.Discount;

public class DiscountMapper {
    public static DiscountDto mapToDiscountDto(Discount discount, DiscountDto discountDto){
        discountDto.setPercentage(discount.getPercentage());
        discountDto.setStartDate(discount.getStartDate());
        discountDto.setEndDate(discount.getEndDate());
        return discountDto;
    }

    public static Discount mapToDiscount(DiscountDto discountDto, Discount discount){
        discount.setPercentage(discountDto.getPercentage());
        discount.setStartDate(discountDto.getStartDate());
        discount.setEndDate(discountDto.getEndDate());
        return discount;
    }
}
