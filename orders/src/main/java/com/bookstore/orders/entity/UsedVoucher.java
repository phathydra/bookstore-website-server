package com.bookstore.orders.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "used_vouchers")
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class UsedVoucher {

    @Id
    private String id;

    private String accountId;

    private List<String> userVoucherCodes;
}
