package com.bookstore.accounts.service.impl;

import com.bookstore.accounts.dto.AccountDto;
import com.bookstore.accounts.dto.EmailDto;
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
import com.bookstore.accounts.service.IEmailService;
import lombok.AllArgsConstructor;

import java.util.Optional;
import java.util.Random;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AccountServiceImpl implements IAccountService {

    private final AccountRepository accountRepository;
    private final InformationRepository informationRepository;
    private final IEmailService emailService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Account createAccount(AccountDto accountDto) {
        Account account = AccountMapper.mapToAccount(accountDto, new Account());

        // Kiểm tra nếu email đã tồn tại trong cơ sở dữ liệu
        Optional<Account> optionalAccount = accountRepository.findByEmail(account.getEmail());
        if (optionalAccount.isPresent()) {
            throw new UsernameAlreadyExistException("Email already exists");
        }

        // Mã hóa mật khẩu trước khi lưu
        account.setPassword(passwordEncoder.encode(account.getPassword()));

        // Gán giá trị mặc định cho quyền và trạng thái
        account.setRole("User");
        account.setStatus("Inactive");

        // Lưu tài khoản và thông tin liên quan
        Account savedAccount = accountRepository.save(account);
        informationRepository.save(createNewInformation(savedAccount));
        return savedAccount;
    }



    private Information createNewInformation(Account account){
        Information newInformation = new Information();
        newInformation.setAccountId(account.getAccountId());
        newInformation.setName("");
        newInformation.setEmail(account.getEmail());
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
            existingAccount.setEmail(accountDto.getEmail());
            existingAccount.setPassword(accountDto.getPassword());
            existingAccount.setRole(accountDto.getRole());
            existingAccount.setStatus(accountDto.getStatus());
            accountRepository.save(existingAccount);
            return true;
        }
        return false;
    }

    @Override
    public boolean checkAdminRole(String username, String password) {
        Optional<Account> account = accountRepository.findByEmailAndPassword(username, password);
        return account.isPresent() && "Admin".equals(account.get().getRole());
    }

    @Override
    public Page<AccountDto> searchAccounts(int page, int size, String input) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Account> accounts = accountRepository.searchAccounts(input, pageable);
        return accounts.map(account -> {
            AccountDto accountDto = new AccountDto();
            accountDto.setAccountId(account.getAccountId());
            accountDto.setEmail(account.getEmail());
            accountDto.setRole(account.getRole());
            accountDto.setStatus(account.getStatus());
            return accountDto;
        });
    }

    @Override
    public Page<InformationDto> searchInformation(int page, int size, String input) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Information> informationPage = informationRepository.searchInformation(input, pageable);
        return informationPage.map(information -> InformationMapper.mapToInformationDto(information, new InformationDto()));
    }
    @Override
    public boolean activateAccount(String accountId) {
        Optional<Account> accountOpt = accountRepository.findById(accountId);
        if (accountOpt.isPresent()) {
            Account account = accountOpt.get();
            account.setStatus("Active");
            accountRepository.save(account);
            return true;
        }
        return false;
    }

    @Override
    public void resetPasswordByEmail(String email) {
        Optional<Account> accountOptional = accountRepository.findByEmail(email);
        if (accountOptional.isPresent()) {
            Account account = accountOptional.get();
            String newPassword = generateStrongPassword();
            String encodedPassword = passwordEncoder.encode(newPassword);
            account.setPassword(encodedPassword);
            accountRepository.save(account);

            // Send email with the new password
            EmailDto emailDto = new EmailDto();
            emailDto.setTo(email);
            emailDto.setSubject("Your New Password");
            emailDto.setContent("Your new password is: <strong>" + newPassword + "</strong><br>Please log in and change your password as soon as possible.");
            emailService.sendEmail(emailDto);
        } else {
            throw new ResourceNotFoundException("Account not found with email: " + email);
        }
    }

    private String generateStrongPassword() {
        String upperCaseLetters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lowerCaseLetters = "abcdefghijklmnopqrstuvwxyz";
        String numbers = "0123456789";
        String specialCharacters = "^&*-_=+;:',.<>";
        String allowedCharacters = upperCaseLetters + lowerCaseLetters + numbers + specialCharacters;
        Random random = new Random();
        StringBuilder password = new StringBuilder();

        // Ensure at least one character from each category
        password.append(upperCaseLetters.charAt(random.nextInt(upperCaseLetters.length())));
        password.append(lowerCaseLetters.charAt(random.nextInt(lowerCaseLetters.length())));
        password.append(numbers.charAt(random.nextInt(numbers.length())));
        password.append(specialCharacters.charAt(random.nextInt(specialCharacters.length())));

        // Generate remaining characters
        int remainingLength = 8 + random.nextInt(8); // Password length between 8 and 15
        for (int i = 4; i < remainingLength; i++) {
            password.append(allowedCharacters.charAt(random.nextInt(allowedCharacters.length())));
        }

        // Shuffle the password to make it more random
        for (int i = password.length() - 1; i > 0; i--) {
            int index = random.nextInt(i + 1);
            char temp = password.charAt(index);
            password.setCharAt(index, password.charAt(i));
            password.setCharAt(i, temp);
        }

        return password.toString();
    }

    @Override
    public boolean changePassword(String accountId, String oldPassword, String newPassword) {
        Optional<Account> accountOptional = accountRepository.findById(accountId);

        if (accountOptional.isEmpty()) {
            throw new ResourceNotFoundException("Account not found with ID: " + accountId);
        }

        Account account = accountOptional.get();

        // So sánh mật khẩu cũ đã gửi với mật khẩu hiện tại đã mã hóa
        if (passwordEncoder.matches(oldPassword, account.getPassword())) {
            // Mã hóa mật khẩu mới và cập nhật vào database
            String encodedNewPassword = passwordEncoder.encode(newPassword);
            account.setPassword(encodedNewPassword);
            accountRepository.save(account);
            return true; // Đổi mật khẩu thành công
        } else {
            return false; // Mật khẩu cũ không đúng
        }
    }
}
