package com.bookstore.accounts.dto;

import lombok.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class AddressDto {

    private String id;
    private String accountId;
    private String phoneNumber;
    private String recipientName; // Tên người nhận
    private String country;
    private String city;
    private String district;
    private String ward;
    private String note;
    private String status; // Trạng thái (ACTIVE hoặc INACTIVE)
}
