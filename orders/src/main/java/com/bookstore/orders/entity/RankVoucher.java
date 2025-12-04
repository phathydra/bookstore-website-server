package com.bookstore.orders.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "rank_vouchers")
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class RankVoucher extends BaseVoucherEntity{
    @Id
    private String id;

    private int rank;
}
