package com.bookstore.accounts.service;

import com.bookstore.accounts.dto.AccountDto;
import com.bookstore.accounts.dto.InformationDto;
import org.springframework.data.domain.Page;

import java.util.List;

public interface IAccountService {
    void createAccount(AccountDto accountDto);

    InformationDto fetchInformation(String accountId);

    boolean updateInformation(String accountId, InformationDto informationDto);

    boolean deleteAccount(String accountId);

    Page<AccountDto> getAllAccounts(int page, int size);

    Page<InformationDto> getAllInformation(int page, int size);

    boolean updateAccount(String accountId, AccountDto accountDto);

    boolean checkAdminRole(String username, String password);

    Page<AccountDto> searchAccounts(int page, int size, String input);

    Page<InformationDto> searchInformation(int page, int size, String input);
}
