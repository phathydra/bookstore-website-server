package com.bookstore.accounts.controller;

import com.bookstore.accounts.dto.EmailDto;
import com.bookstore.accounts.service.IEmailService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/email")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"}, exposedHeaders = "Content-Disposition")
@AllArgsConstructor
public class EmailController {

    private final IEmailService emailService;

    @PostMapping("/send")
    public ResponseEntity<?> sendEmail(@RequestBody EmailDto emailDto) {
        boolean result = emailService.sendEmail(emailDto);
        if (result) {
            return ResponseEntity.ok("Email sent successfully");
        } else {
            return ResponseEntity.status(500).body("Failed to send email");
        }
    }
}
