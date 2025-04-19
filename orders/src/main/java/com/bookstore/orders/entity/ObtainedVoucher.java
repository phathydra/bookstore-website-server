package com.bookstore.orders.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "obtained_voucher")
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class ObtainedVoucher{

    @Id
    private String id;

    private String userId;

    private List<String> obtainedVouchers;
}
