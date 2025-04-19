package com.bookstore.orders.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "obtainable_voucher")
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class ObtainableVoucher extends BaseVoucherEntity{

    @Id
    private String id;

    // true if voucher can be claimed without requirement
    // false if voucher need the match the requirement to be claimed after place order
    private boolean publicClaimable;
    private Double valueRequirement;
}
