package com.bookstore.orders.mapper;

import com.bookstore.orders.dto.ObtainableVoucherDto;
import com.bookstore.orders.dto.OrderVoucherDto;
import com.bookstore.orders.dto.VoucherDto;
import com.bookstore.orders.entity.ObtainableVoucher;
import com.bookstore.orders.entity.ObtainedVoucher;
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
        voucher.setUserUsageLimit(voucherDto.getUserUsageLimit());
        voucher.setPublish(voucherDto.isPublish());
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
        voucherDto.setUserUsageLimit(voucher.getUserUsageLimit());
        voucherDto.setPublish(voucher.isPublish());
        voucherDto.setStartDate(voucher.getStartDate());
        voucherDto.setEndDate(voucher.getEndDate());
        return voucherDto;
    }

    static public ObtainableVoucher toObtainableVoucher(ObtainableVoucherDto voucherDto, ObtainableVoucher voucher){
        voucher.setCode(voucherDto.getCode());
        voucher.setVoucherType(voucherDto.getVoucherType());
        voucher.setPercentageDiscount(voucherDto.getPercentageDiscount());
        voucher.setValueDiscount(voucherDto.getValueDiscount());
        voucher.setHighestDiscountValue(voucherDto.getHighestDiscountValue());
        voucher.setMinOrderValue(voucherDto.getMinOrderValue());
        voucher.setUsageLimit(voucherDto.getUsageLimit());
        voucher.setPublicClaimable(voucherDto.isPublicClaimable());
        voucher.setValueRequirement(voucherDto.getValueRequirement());
        voucher.setStartDate(voucherDto.getStartDate());
        voucher.setEndDate(voucherDto.getEndDate());
        return voucher;
    }

    static public ObtainableVoucherDto toObtainableVoucherDto(ObtainableVoucher voucher, ObtainableVoucherDto voucherDto){
        voucherDto.setId(voucher.getId());
        voucherDto.setCode(voucher.getCode());
        voucherDto.setVoucherType(voucher.getVoucherType());
        voucherDto.setPercentageDiscount(voucher.getPercentageDiscount());
        voucherDto.setValueDiscount(voucher.getValueDiscount());
        voucherDto.setHighestDiscountValue(voucher.getHighestDiscountValue());
        voucherDto.setMinOrderValue(voucher.getMinOrderValue());
        voucherDto.setUsageLimit(voucher.getUsageLimit());
        voucherDto.setPublicClaimable(voucher.isPublicClaimable());
        voucherDto.setValueRequirement(voucher.getValueRequirement());
        voucherDto.setStartDate(voucher.getStartDate());
        voucherDto.setEndDate(voucher.getEndDate());
        return voucherDto;
    }

    static public OrderVoucher toOrderVoucher(OrderVoucherDto orderVoucherDto, OrderVoucher orderVoucher){
        orderVoucher.setOrderId(orderVoucherDto.getOrderId());
        orderVoucher.setVoucherCode(orderVoucherDto.getVoucherCode());
        orderVoucher.setDiscountedPrice(orderVoucherDto.getDiscountedPrice());
        return orderVoucher;
    }

    static public OrderVoucherDto toOrderVoucherDto(OrderVoucher orderVoucher, OrderVoucherDto orderVoucherDto){
        orderVoucherDto.setOrderId(orderVoucher.getOrderId());
        orderVoucherDto.setVoucherCode(orderVoucher.getVoucherCode());
        orderVoucherDto.setDiscountedPrice(orderVoucher.getDiscountedPrice());
        return orderVoucherDto;
    }
}
