package com.bookstore.accounts.service.impl;

import com.bookstore.accounts.dto.EmailDto;
import com.bookstore.accounts.entity.Email;
import com.bookstore.accounts.repository.EmailRepository;
import com.bookstore.accounts.service.IEmailService;
import jakarta.mail.MessagingException;
import lombok.AllArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;

@Service
@AllArgsConstructor
public class EmailServiceImpl implements IEmailService {

    private final JavaMailSender mailSender;
    private final EmailRepository emailRepository;

    @Override
    public boolean sendEmail(EmailDto emailDto) {
        Email email = new Email();
        email.setTo(emailDto.getTo());
        email.setSubject(emailDto.getSubject());
        email.setContent(emailDto.getContent());

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "utf-8");
            helper.setTo(emailDto.getTo());
            helper.setSubject(emailDto.getSubject());
            helper.setText(emailDto.getContent(), true);

            mailSender.send(message);

            email.setStatus("Success");
            emailRepository.save(email);
            return true;
        } catch (MessagingException e) {
            email.setStatus("Failed");
            emailRepository.save(email);
            e.printStackTrace();
            return false;
        }
    }
}
