package com.bookstore.accounts.service;

import com.bookstore.accounts.dto.AccountDto;
import com.bookstore.accounts.dto.InformationDto;
import com.bookstore.accounts.entity.Account;
import org.springframework.data.domain.Page;

public interface IAccountService {
    Account createAccount(AccountDto accountDto);

    InformationDto fetchInformation(String accountId);

    boolean updateInformation(String accountId, InformationDto informationDto);

    boolean deleteAccount(String accountId);

    Page<AccountDto> getAllAccounts(int page, int size);

    Page<InformationDto> getAllInformation(int page, int size);

    boolean updateAccount(String accountId, AccountDto accountDto);

    boolean checkAdminRole(String username, String password);

    Page<AccountDto> searchAccounts(int page, int size, String input);

    Page<InformationDto> searchInformation(int page, int size, String input);

    boolean activateAccount(String accountId);

    void resetPasswordByEmail(String email);

    boolean changePassword(String accountId, String oldPassword, String newPassword);
}
