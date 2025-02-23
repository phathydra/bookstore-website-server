package com.bookstore.accounts.dto;

import lombok.Data;

@Data
public class AccountDto {

    private Long accountId;

    private String username;

    private String password;

    private String role;
}
