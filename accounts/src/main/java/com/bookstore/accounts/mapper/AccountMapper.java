package com.bookstore.accounts.mapper;

import com.bookstore.accounts.dto.AccountDto;
import com.bookstore.accounts.entity.Account;

public class AccountMapper {

    // Map từ Account -> AccountDto (với DTO đã có sẵn)
    public static AccountDto mapToAccountDto(Account account, AccountDto accountDto) {
        accountDto.setAccountId(account.getAccountId());
        accountDto.setEmail(account.getEmail());
        accountDto.setPassword(account.getPassword());
        accountDto.setRole(account.getRole());
        accountDto.setStatus(account.getStatus());
        return accountDto;
    }

    // Map từ Account -> AccountDto (tạo DTO mới)
    public static AccountDto mapToAccountDto(Account account) {
        return mapToAccountDto(account, new AccountDto());
    }

    // Map từ AccountDto -> Account (với entity đã có sẵn)
    public static Account mapToAccount(AccountDto accountDto, Account account){
        account.setEmail(accountDto.getEmail());
        account.setPassword(accountDto.getPassword());
        account.setRole(accountDto.getRole());
        account.setStatus(accountDto.getStatus());
        return account;
    }

    // Map từ AccountDto -> Account (tạo entity mới)
    public static Account mapToAccount(AccountDto accountDto) {
        return mapToAccount(accountDto, new Account());
    }
}
