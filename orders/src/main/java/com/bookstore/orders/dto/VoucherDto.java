package com.bookstore.orders.dto;

import lombok.Data;

import java.util.Date;

@Data
public class VoucherDto {

    private String id;

    private String code;

    private String voucherType;

    private int percentageDiscount;
    private Double valueDiscount;
    private Double highestDiscountValue;

    private Double minOrderValue;
    private int usageLimit;

    private Date startDate;
    private Date endDate;
}
