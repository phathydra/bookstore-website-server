package com.bookstore.orders.mapper;

import com.bookstore.orders.dto.VoucherDto;
import com.bookstore.orders.entity.Voucher;
import org.springframework.stereotype.Component;

@Component
public class VoucherMapper {
    public Voucher toVoucher(VoucherDto voucherDto, Voucher voucher){
        voucher.setCode(voucherDto.getCode());
        voucher.setVoucherType(voucherDto.getVoucherType());
        voucher.setPercentageDiscount(voucherDto.getPercentageDiscount());
        voucher.setValueDiscount(voucherDto.getValueDiscount());
        voucher.setHighestDiscountValue(voucherDto.getHighestDiscountValue());
        voucher.setMinOrderValue(voucherDto.getMinOrderValue());
        voucher.setUsageLimit(voucherDto.getUsageLimit());
        voucher.setStartDate(voucherDto.getStartDate());
        voucher.setEndDate(voucherDto.getEndDate());
        return voucher;
    }

    public VoucherDto toVoucherDto(Voucher voucher, VoucherDto voucherDto){
        voucherDto.setCode(voucher.getCode());
        voucherDto.setVoucherType(voucher.getVoucherType());
        voucherDto.setPercentageDiscount(voucher.getPercentageDiscount());
        voucherDto.setValueDiscount(voucher.getValueDiscount());
        voucherDto.setHighestDiscountValue(voucher.getHighestDiscountValue());
        voucherDto.setMinOrderValue(voucher.getMinOrderValue());
        voucherDto.setUsageLimit(voucher.getUsageLimit());
        voucherDto.setStartDate(voucher.getStartDate());
        voucherDto.setEndDate(voucher.getEndDate());
        return voucherDto;
    }
}
