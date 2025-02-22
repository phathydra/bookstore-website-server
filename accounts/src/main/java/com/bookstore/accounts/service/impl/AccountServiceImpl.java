package com.bookstore.accounts.service.impl;

import com.bookstore.accounts.dto.AccountDto;
import com.bookstore.accounts.dto.InformationDto;
import com.bookstore.accounts.entity.Account;
import com.bookstore.accounts.entity.Information;
import com.bookstore.accounts.exception.UsernameAlreadyExistException;
import com.bookstore.accounts.mapper.AccountMapper;
import com.bookstore.accounts.mapper.InformationMapper;
import com.bookstore.accounts.repository.AccountRepository;
import com.bookstore.accounts.repository.InformationRepository;
import com.bookstore.accounts.service.IAccountService;
import lombok.AllArgsConstructor;

import java.util.Optional;

import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AccountServiceImpl implements IAccountService {

    private AccountRepository accountRepository;
    private InformationRepository informationRepository;

    @Override
    public void createAccount(AccountDto accountDto) {
        Account account = AccountMapper.mapToAccount(accountDto, new Account());
        account.setUsername("test");
        Optional<Account> optionalAccount = accountRepository.findByUsername(account.getUsername());
        if(optionalAccount.isPresent()){
            throw new UsernameAlreadyExistException("Username already exist");
        }
        account.setPassword("test");
        account.setRole("Customers");
        Account savedAccount = accountRepository.save(account);
        informationRepository.save(createNewInformation(savedAccount));
    }

    private Information createNewInformation(Account account){
        Information newInformation = new Information();
        newInformation.setAccountId(account.getAccountId());
        newInformation.setName("test");
        newInformation.setEmail("test@gmail.com");
        newInformation.setPhone("123456");
        newInformation.setAddress("1 street");
        newInformation.setAvatar(null);
        return newInformation;
    }
}
