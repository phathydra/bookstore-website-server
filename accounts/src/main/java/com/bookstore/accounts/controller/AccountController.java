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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CrossOrigin("*")
@RestController
@RequestMapping(path = "/api/account", produces = {MediaType.APPLICATION_JSON_VALUE})
@AllArgsConstructor
public class AccountController {

    @Autowired
    private AccountRepository accountRepository;
    private IAccountService iAccountService;

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody AccountDto accountDto) {
        Account account = accountRepository.findByUsernameAndPassword(accountDto.getUsername(), accountDto.getPassword());

        Map<String, Object> response = new HashMap<>();
        if (account != null) {
            response.put("statusCode", "200");
            response.put("statusMsg", "Login successful");
            response.put("accountId", account.getAccountId()); // Chỉ trả về accountId

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
    public ResponseEntity<List<AccountDto>> fetchAllAccounts() {
        List<AccountDto> accounts = iAccountService.getAllAccounts();
        return ResponseEntity.status(HttpStatus.OK).body(accounts);
    }

    @GetMapping("/allInformation")
    public ResponseEntity<List<InformationDto>> fetchAllInformation() {
        List<InformationDto> informationList = iAccountService.getAllInformation();
        return ResponseEntity.status(HttpStatus.OK).body(informationList);
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

}
