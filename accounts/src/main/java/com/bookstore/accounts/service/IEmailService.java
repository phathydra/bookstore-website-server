package com.bookstore.accounts.service;

import com.bookstore.accounts.dto.EmailDto;

public interface IEmailService {
    boolean sendEmail(EmailDto emailDto);
}
