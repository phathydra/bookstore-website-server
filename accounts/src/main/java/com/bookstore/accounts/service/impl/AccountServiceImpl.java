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

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AccountServiceImpl implements IAccountService {

    private final AccountRepository accountRepository;
    private final InformationRepository informationRepository;

    @Override
    public void createAccount(AccountDto accountDto) {
        Account account = AccountMapper.mapToAccount(accountDto, new Account());
        Optional<Account> optionalAccount = accountRepository.findByUsername(account.getUsername());
        if(optionalAccount.isPresent()){
            throw new UsernameAlreadyExistException("Username already exists");
        }
        Account savedAccount = accountRepository.save(account);
        informationRepository.save(createNewInformation(savedAccount));
    }

    private Information createNewInformation(Account account){
        Information newInformation = new Information();
        newInformation.setAccountId(account.getAccountId());
        newInformation.setName("");
        newInformation.setEmail("");
        newInformation.setPhone("");
        newInformation.setAddress("");
        newInformation.setAvatar(null);
        return newInformation;
    }

    @Override
    public InformationDto fetchInformation(String accountId){
        Information information = informationRepository.findByAccountId(accountId).orElseThrow(
                () -> new ResourceNotFoundException("Cannot find account information")
        );

        return InformationMapper.mapToInformationDto(information, new InformationDto());
    }

    @Override
    public boolean updateInformation(String accountId, InformationDto informationDto) {
        Information information = informationRepository.findByAccountId(accountId).orElseThrow(
                () -> new ResourceNotFoundException("Account not found")
        );
        InformationMapper.mapToInformation(informationDto, information);
        informationRepository.save(information);
        return true;
    }

    @Override
    public boolean deleteAccount(String accountId) {
        Account account = accountRepository.findById(accountId).orElseThrow(
                () -> new ResourceNotFoundException("Account not found")
        );

        informationRepository.findByAccountId(accountId).ifPresent(informationRepository::delete);
        accountRepository.delete(account);

        return true;
    }


    @Override
    public Page<AccountDto> getAllAccounts(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Account> accounts = accountRepository.findAll(pageable);
        return accounts.map(account -> AccountMapper.mapToAccountDto(account, new AccountDto()));
    }

    @Override
    public Page<InformationDto> getAllInformation(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Information> informationList = informationRepository.findAll(pageable);
        return informationList.map(info -> InformationMapper.mapToInformationDto(info, new InformationDto()));
    }

    @Override
    public boolean updateAccount(String accountId, AccountDto accountDto) {
        Optional<Account> existingAccountOpt = accountRepository.findById(accountId);
        if (existingAccountOpt.isPresent()) {
            Account existingAccount = existingAccountOpt.get();
            existingAccount.setUsername(accountDto.getUsername());
            existingAccount.setPassword(accountDto.getPassword());
            existingAccount.setRole(accountDto.getRole());
            accountRepository.save(existingAccount);
            return true;
        }
        return false;
    }

    @Override
    public boolean checkAdminRole(String username, String password) {
        Optional<Account> account = accountRepository.findByUsernameAndPassword(username, password);
        return account.isPresent() && "Admin".equals(account.get().getRole());
    }

    @Override
    public Page<AccountDto> searchAccounts(int page, int size, String input) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Account> accounts = accountRepository.searchAccounts(input, pageable);
        return accounts.map(account -> {
            AccountDto accountDto = new AccountDto();
            accountDto.setAccountId(account.getAccountId());
            accountDto.setUsername(account.getUsername());
            accountDto.setRole(account.getRole());
            return accountDto;
        });
    }

    @Override
    public Page<InformationDto> searchInformation(int page, int size, String input) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Information> informationPage = informationRepository.searchInformation(input, pageable);
        return informationPage.map(information -> InformationMapper.mapToInformationDto(information, new InformationDto()));
    }
}
