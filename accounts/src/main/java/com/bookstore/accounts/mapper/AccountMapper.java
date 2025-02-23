package com.bookstore.accounts.mapper;

import com.bookstore.accounts.dto.AccountDto;
import com.bookstore.accounts.entity.Account;

public class AccountMapper {
    public static AccountDto mapToAccountDto(Account account, AccountDto accountDto) {
        accountDto.setAccountId(account.getAccountId()); // Thêm dòng này
        accountDto.setUsername(account.getUsername());
        accountDto.setPassword(account.getPassword());
        accountDto.setRole(account.getRole());
        return accountDto;
    }

    public static Account mapToAccount(AccountDto accountDto, Account account){
        account.setUsername(accountDto.getUsername());
        account.setPassword(accountDto.getPassword());
        account.setRole(accountDto.getRole());
        return account;
    }
}
