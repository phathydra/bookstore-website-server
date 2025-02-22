package com.bookstore.accounts.service;

import com.bookstore.accounts.dto.AccountDto;
import com.bookstore.accounts.dto.InformationDto;
import com.bookstore.accounts.entity.Information;

public interface IAccountService {
    void createAccount(AccountDto accountDto);

    InformationDto fetchInformation(Long accountId);

    boolean updateInformation(Long accountId, InformationDto informationDto);

    boolean deleteAccount(Long accountId);
}
