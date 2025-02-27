package com.bookstore.accounts.dto;

import lombok.Data;

@Data
public class AccountDto {

    private String accountId;

    private String username;

    private String password;

    private String role;
}
