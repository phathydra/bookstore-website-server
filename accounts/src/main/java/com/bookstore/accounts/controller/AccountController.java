package com.bookstore.accounts.controller;

import com.bookstore.accounts.constants.AccountConstants;
import com.bookstore.accounts.dto.AccountDto;
import com.bookstore.accounts.dto.InformationDto;
import com.bookstore.accounts.dto.ResponseDto;
import com.bookstore.accounts.entity.Account;
import com.bookstore.accounts.repository.AccountRepository;
import com.bookstore.accounts.service.IAccountService;

import lombok.AllArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@CrossOrigin("*")
@RestController
@RequestMapping(path = "/api/account", produces = {MediaType.APPLICATION_JSON_VALUE})
@AllArgsConstructor
public class AccountController {

    @Autowired
    private AccountRepository accountRepository;
    private IAccountService iAccountService;
    private final IAccountService accountService;

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody AccountDto accountDto) {
        Optional<Account> account = accountRepository.findByUsernameAndPassword(accountDto.getUsername(), accountDto.getPassword());

        Map<String, Object> response = new HashMap<>();
        if (account != null) {
            response.put("statusCode", "200");
            response.put("statusMsg", "Login successful");
            response.put("accountId", account.get().getAccountId()); // Chỉ trả về accountId

            return ResponseEntity.status(HttpStatus.OK).body(response);
        } else {
            response.put("statusCode", "401");
            response.put("statusMsg", "Invalid credentials");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }




    @PostMapping("/create")
    public ResponseEntity<ResponseDto> createAccount(@RequestBody AccountDto accountDto){
        iAccountService.createAccount(accountDto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ResponseDto(AccountConstants.STATUS_201, AccountConstants.MESSAGE_201));
    }

    @GetMapping("/fetch")
    public ResponseEntity<InformationDto> fetchAccountInformation(@RequestParam String accountId){
        InformationDto informationDto = iAccountService.fetchInformation(accountId);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(informationDto);
    }

    @PutMapping("/update-information")
    public  ResponseEntity<ResponseDto> updateInformation(@RequestParam String accountId, @RequestBody InformationDto informationDto){
        boolean isUpdated = iAccountService.updateInformation(accountId, informationDto);
        if(isUpdated){
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(new ResponseDto(AccountConstants.STATUS_200, AccountConstants.MESSAGE_200));
        }
        else{
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDto(AccountConstants.STATUS_500, AccountConstants.MESSAGE_500));
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<ResponseDto> deleteAccount(@RequestParam String accountId){
        boolean isDeleted = iAccountService.deleteAccount(accountId);
        if(isDeleted){
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(new ResponseDto(AccountConstants.STATUS_200, AccountConstants.MESSAGE_200));
        }
        else{
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDto(AccountConstants.STATUS_500, AccountConstants.MESSAGE_500));
        }
    }

    @GetMapping("/allAccount")
    public ResponseEntity<Page<AccountDto>> fetchAllAccounts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Page<AccountDto> accounts = iAccountService.getAllAccounts(page, size);
            return ResponseEntity.ok(accounts);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Page.empty());
        }
    }

    @GetMapping("/allInformation")
    public ResponseEntity<Page<InformationDto>> fetchAllInformation(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Page<InformationDto> informationList = iAccountService.getAllInformation(page, size);
            return ResponseEntity.ok(informationList);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Page.empty());
        }
    }

    @PutMapping("/update-account")
    public ResponseEntity<ResponseDto> updateAccount(@RequestParam String accountId, @RequestBody AccountDto accountDto) {
        boolean isUpdated = iAccountService.updateAccount(accountId, accountDto);
        if (isUpdated) {
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(new ResponseDto(AccountConstants.STATUS_200, AccountConstants.MESSAGE_200));
        } else {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDto(AccountConstants.STATUS_500, AccountConstants.MESSAGE_500));
        }
    }

    @PostMapping("/account_search")
    public ResponseEntity<Page<AccountDto>> searchAccounts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam String input) {
        try {
            Page<AccountDto> accounts = accountService.searchAccounts(page, size, input); // ✅ Gọi từ instance
            return ResponseEntity.ok(accounts);
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Page.empty());
        }
    }
    @PostMapping("/information_search")
    public ResponseEntity<Page<InformationDto>> searchInformation(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam String input) {
        try {
            Page<InformationDto> informationDtoPage = accountService.searchInformation(page, size, input);
            return ResponseEntity.ok(informationDtoPage);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Page.empty());
        }
    }
}
