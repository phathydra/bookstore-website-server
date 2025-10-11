package com.bookstore.accounts.controller;

import com.bookstore.accounts.constants.AccountConstants;
import com.bookstore.accounts.dto.AccountDto;
import com.bookstore.accounts.dto.InformationDto;
import com.bookstore.accounts.dto.ResponseDto;
import com.bookstore.accounts.entity.Account;
import com.bookstore.accounts.exception.UsernameAlreadyExistException;
import com.bookstore.accounts.repository.AccountRepository;
import com.bookstore.accounts.service.IAccountService;

import org.springframework.core.io.Resource;
import org.springframework.core.io.InputStreamResource;

import lombok.AllArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001", "http://localhost:3002"}, exposedHeaders = "Content-Disposition")
@RestController
@RequestMapping(path = "/api/account", produces = {MediaType.APPLICATION_JSON_VALUE})
@AllArgsConstructor
public class AccountController {

    @Autowired
    private AccountRepository accountRepository;
    private IAccountService iAccountService;
    private final IAccountService accountService;
    private PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody AccountDto accountDto) {
        Optional<Account> accountOpt = accountRepository.findByEmail(accountDto.getEmail());

        Map<String, Object> response = new HashMap<>();

        if (accountOpt.isPresent()) {
            Account account = accountOpt.get();

            // So sánh password đã mã hóa
            if (passwordEncoder.matches(accountDto.getPassword(), account.getPassword())) {

                if ("Active".equalsIgnoreCase(account.getStatus())) {
                    response.put("statusCode", "200");
                    response.put("statusMsg", "Login successful");
                    response.put("accountId", account.getAccountId());
                    response.put("role", account.getRole());
                    return ResponseEntity.status(HttpStatus.OK).body(response);
                } else {
                    response.put("statusCode", "403");
                    response.put("statusMsg", "Account is inactive");
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
                }

            } else {
                response.put("statusCode", "401");
                response.put("statusMsg", "Invalid credentials");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

        } else {
            response.put("statusCode", "401");
            response.put("statusMsg", "Invalid credentials");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }

    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createAccount(@RequestBody AccountDto accountDto) {
        try {
            Account createdAccount = iAccountService.createAccount(accountDto);
            Map<String, Object> response = new HashMap<>();
            response.put("statusCode", AccountConstants.STATUS_201);
            response.put("statusMsg", AccountConstants.MESSAGE_201);
            response.put("accountId", createdAccount.getAccountId()); // Thêm accountId vào response
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(response);
        } catch (UsernameAlreadyExistException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("statusCode", AccountConstants.STATUS_400);
            errorResponse.put("statusMsg", "Email already exists");
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(errorResponse);
        }
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
    @PutMapping("/activate")
    public ResponseEntity<ResponseDto> activateAccount(@RequestParam String accountId) {
        boolean isActivated = iAccountService.activateAccount(accountId);
        if (isActivated) {
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(new ResponseDto(AccountConstants.STATUS_200, "Account activated successfully"));
        } else {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(new ResponseDto(AccountConstants.STATUS_404, "Account not found"));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ResponseDto> resetPassword(@RequestParam String email) {
        try {
            accountService.resetPasswordByEmail(email);
            ResponseDto responseDto = new ResponseDto("Password reset successfully. Please check your email.", HttpStatus.OK.toString());
            return new ResponseEntity<>(responseDto, HttpStatus.OK);
        } catch (Exception e) {
            ResponseDto responseDto = new ResponseDto(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.toString());
            return new ResponseEntity<>(responseDto, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/change-password")
    public ResponseEntity<ResponseDto> changePassword(
            @RequestParam String accountId,
            @RequestParam String oldPassword,
            @RequestParam String newPassword) {
        boolean isChanged = accountService.changePassword(accountId, oldPassword, newPassword);
        if (isChanged) {
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(new ResponseDto(AccountConstants.STATUS_200, "Password changed successfully"));
        } else {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ResponseDto(AccountConstants.STATUS_400, "Incorrect old password"));
        }
    }

    @GetMapping("/export_accounts")
    public ResponseEntity<Resource> exportAccounts() throws IOException {
        // gọi service để lấy file Excel dưới dạng ByteArrayInputStream
        ByteArrayInputStream in = accountService.exportAccounts();

        // gói vào InputStreamResource
        InputStreamResource file = new InputStreamResource(in);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=accounts.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
                .body(file);
    }

    @GetMapping("/export_informations")
    public ResponseEntity<Resource> exportInformations() throws IOException {
        AccountController informationService;
        ByteArrayInputStream in = accountService.exportInformations();
        InputStreamResource file = new InputStreamResource(in);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=informations.xlsx")
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(file);
    }

}
