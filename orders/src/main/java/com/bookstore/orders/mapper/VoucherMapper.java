package com.bookstore.orders.mapper;

import com.bookstore.orders.dto.OrderVoucherDto;
import com.bookstore.orders.dto.VoucherDto;
import com.bookstore.orders.entity.OrderVoucher;
import com.bookstore.orders.entity.Voucher;
import org.springframework.stereotype.Component;

@Component
public class VoucherMapper {
    static public Voucher toVoucher(VoucherDto voucherDto, Voucher voucher){
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

    static public VoucherDto toVoucherDto(Voucher voucher, VoucherDto voucherDto){
        voucherDto.setId(voucher.getId());
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

    static public OrderVoucher toOrderVoucher(OrderVoucherDto orderVoucherDto, OrderVoucher orderVoucher){
        orderVoucher.setOrderId(orderVoucherDto.getOrderId());
        orderVoucher.setVoucherId(orderVoucherDto.getVoucherId());
        orderVoucher.setDiscountedPrice(orderVoucherDto.getDiscountedPrice());
        return orderVoucher;
    }

    static public OrderVoucherDto toOrderVoucherDto(OrderVoucher orderVoucher, OrderVoucherDto orderVoucherDto){
        orderVoucherDto.setOrderId(orderVoucher.getOrderId());
        orderVoucherDto.setVoucherId(orderVoucher.getVoucherId());
        orderVoucherDto.setDiscountedPrice(orderVoucher.getDiscountedPrice());
        return orderVoucherDto;
    }
}
