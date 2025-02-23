package com.bookstore.accounts.service.impl;

import com.bookstore.accounts.dto.AccountDto;
import com.bookstore.accounts.dto.InformationDto;
import com.bookstore.accounts.entity.Account;
import com.bookstore.accounts.entity.Information;
import com.bookstore.accounts.exception.ResourceNotFoundException;
import com.bookstore.accounts.exception.UsernameAlreadyExistException;
import com.bookstore.accounts.mapper.AccountMapper;
import com.bookstore.accounts.mapper.InformationMapper;
import com.bookstore.accounts.repository.AccountRepository;
import com.bookstore.accounts.repository.InformationRepository;
import com.bookstore.accounts.service.IAccountService;
import lombok.AllArgsConstructor;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AccountServiceImpl implements IAccountService {

    private AccountRepository accountRepository;
    private InformationRepository informationRepository;

    @Override
    public void createAccount(AccountDto accountDto) {
        Account account = AccountMapper.mapToAccount(accountDto, new Account());
        Optional<Account> optionalAccount = accountRepository.findByUsername(account.getUsername());
        if(optionalAccount.isPresent()){
            throw new UsernameAlreadyExistException("Username already exist");
        }
        account.setCreatedBy("me");
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
        newInformation.setCreatedBy("me");
        return newInformation;
    }

    @Override
    public InformationDto fetchInformation(Long accountId){
        Information information = informationRepository.findByAccountId(accountId).orElseThrow(
                () -> new ResourceNotFoundException("Cannot find account information")
        );

        return InformationMapper.mapToInformationDto(information, new InformationDto());
    }

    @Override
    public boolean updateInformation(Long accountId, InformationDto informationDto) {
        Information information = informationRepository.findByAccountId(accountId).orElseThrow(
                () -> new ResourceNotFoundException("Account not existed")
        );
        InformationMapper.mapToInformation(informationDto, information);
        informationRepository.save(information);
        return true;
    }

    @Override
    public boolean deleteAccount(Long accountId) {
        Account account = accountRepository.findById(accountId).orElseThrow(
                () ->  new ResourceNotFoundException("Account not existed")
        );
        informationRepository.deleteByAccountId(accountId);
        accountRepository.deleteById(accountId);
        return true;
    }

    @Override
    public List<AccountDto> getAllAccounts() {
        List<Account> accounts = accountRepository.findAll();
        return accounts.stream()
                .map(account -> AccountMapper.mapToAccountDto(account, new AccountDto()))
                .collect(Collectors.toList());
    }

    @Override
    public List<InformationDto> getAllInformation() {
        List<Information> informationList = informationRepository.findAll();
        return informationList.stream()
                .map(info -> InformationMapper.mapToInformationDto(info, new InformationDto()))
                .collect(Collectors.toList());
    }
}
