package com.bookstore.accounts.controller;

import com.bookstore.accounts.constants.AccountConstants;
import com.bookstore.accounts.dto.AccountDto;
import com.bookstore.accounts.dto.InformationDto;
import com.bookstore.accounts.dto.ResponseDto;
import com.bookstore.accounts.service.IAccountService;

import lombok.AllArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin("*")
@RestController
@RequestMapping(path = "/api/account", produces = {MediaType.APPLICATION_JSON_VALUE})
@AllArgsConstructor
public class AccountController {

    private IAccountService iAccountService;

    @PostMapping("/create")
    public ResponseEntity<ResponseDto> createAccount(@RequestBody AccountDto accountDto){
        iAccountService.createAccount(accountDto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ResponseDto(AccountConstants.STATUS_201, AccountConstants.MESSAGE_201));
    }

    @GetMapping("/fetch")
    public ResponseEntity<InformationDto> fetchAccountInformation(@RequestParam Long accountId){
        InformationDto informationDto = iAccountService.fetchInformation(accountId);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(informationDto);
    }

    @PutMapping("/update")
    public  ResponseEntity<ResponseDto> updateInformation(@RequestParam Long accountId, @RequestBody InformationDto informationDto){
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
    public ResponseEntity<ResponseDto> deleteAccount(@RequestParam Long accountId){
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

}
