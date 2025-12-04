package com.bookstore.orders.mapper;

import com.bookstore.orders.dto.ObtainableVoucherDto;
import com.bookstore.orders.dto.OrderVoucherDto;
import com.bookstore.orders.dto.RankVoucherDto;
import com.bookstore.orders.dto.VoucherDto;
import com.bookstore.orders.entity.ObtainableVoucher;
import com.bookstore.orders.entity.OrderVoucher;
import com.bookstore.orders.entity.RankVoucher;
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

    static public RankVoucher toRankVoucher(RankVoucherDto rankVoucherDto, RankVoucher rankVoucher){
        rankVoucher.setCode(rankVoucherDto.getCode());
        rankVoucher.setVoucherType(rankVoucherDto.getVoucherType());
        rankVoucher.setPercentageDiscount(rankVoucherDto.getPercentageDiscount());
        rankVoucher.setValueDiscount(rankVoucherDto.getValueDiscount());
        rankVoucher.setHighestDiscountValue(rankVoucherDto.getHighestDiscountValue());
        rankVoucher.setMinOrderValue(rankVoucherDto.getMinOrderValue());
        rankVoucher.setUsageLimit(rankVoucherDto.getUsageLimit());
        rankVoucher.setStartDate(rankVoucherDto.getStartDate());
        rankVoucher.setEndDate(rankVoucherDto.getEndDate());
        rankVoucher.setRank(rankVoucherDto.getRank());
        return rankVoucher;
    }

    static public RankVoucherDto toRankVoucherDto(RankVoucher rankVoucher, RankVoucherDto rankVoucherDto){
        rankVoucherDto.setId(rankVoucher.getId());
        rankVoucherDto.setCode(rankVoucher.getCode());
        rankVoucherDto.setVoucherType(rankVoucher.getVoucherType());
        rankVoucherDto.setPercentageDiscount(rankVoucher.getPercentageDiscount());
        rankVoucherDto.setValueDiscount(rankVoucher.getValueDiscount());
        rankVoucherDto.setHighestDiscountValue(rankVoucher.getHighestDiscountValue());
        rankVoucherDto.setMinOrderValue(rankVoucher.getMinOrderValue());
        rankVoucherDto.setUsageLimit(rankVoucher.getUsageLimit());
        rankVoucherDto.setStartDate(rankVoucher.getStartDate());
        rankVoucherDto.setEndDate(rankVoucher.getEndDate());
        rankVoucherDto.setRank(rankVoucher.getRank());
        return rankVoucherDto;
    }
}
