package com.bookstore.orders.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.util.Date;

@Document(collection = "vouchers")
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class Voucher {

    @Id
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
