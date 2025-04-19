package com.bookstore.orders.entity;

import lombok.*;

import java.util.Date;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class BaseVoucherEntity {
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
