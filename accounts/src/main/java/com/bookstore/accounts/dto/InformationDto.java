package com.bookstore.accounts.dto;

import lombok.Data;

@Data
public class InformationDto {

    private String id;
    private String accountId;

    private String name;

    private String email;

    private String phone;

    private String address;

    private String avatar;
}
