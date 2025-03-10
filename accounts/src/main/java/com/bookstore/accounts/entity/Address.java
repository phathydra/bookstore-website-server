package com.bookstore.accounts.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "addresses")
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class Address {

    @Id
    private String id;

    private String accountId; // ID của tài khoản

    private String phoneNumber; // Số điện thoại

    private String recipientName; // Tên người nhận

    private String country; // Quốc gia

    private String city; // Thành phố / Tỉnh

    private String district; // Quận / Huyện

    private String ward; // Phường / Xã

    private String note; // Ghi chú địa chỉ

    private String status; // Trạng thái (ACTIVE hoặc INACTIVE)
}
