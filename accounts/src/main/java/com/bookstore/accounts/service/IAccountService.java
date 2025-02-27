package com.bookstore.accounts.service;

import com.bookstore.accounts.dto.AccountDto;
import com.bookstore.accounts.dto.InformationDto;

import java.util.List;

public interface IAccountService {
    void createAccount(AccountDto accountDto);

    InformationDto fetchInformation(String accountId);

    boolean updateInformation(String accountId, InformationDto informationDto);

    boolean deleteAccount(String accountId);

    List<AccountDto> getAllAccounts();

    List<InformationDto> getAllInformation();

    boolean updateAccount(String accountId, AccountDto accountDto);

    boolean checkAdminRole(String username, String password);
}
