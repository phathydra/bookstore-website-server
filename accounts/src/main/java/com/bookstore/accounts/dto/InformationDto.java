package com.bookstore.accounts.dto;

import lombok.Data;

@Data
public class InformationDto {

    private Long id;
    private Long accountId;

    private String name;

    private String email;

    private String phone;

    private String address;

    private String avatar;
}
