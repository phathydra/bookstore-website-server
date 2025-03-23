package com.tlcn.books.entity;

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
    String id;
    String code;
    Condition condition;
    int usedTimes;
    String voucherType;
    Double lowestPriceApply;
    Double highestDiscount;
    int discount;

    Date startDate;
    Date endDate;
}
