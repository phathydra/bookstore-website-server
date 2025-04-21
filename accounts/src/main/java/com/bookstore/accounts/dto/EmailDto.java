package com.bookstore.accounts.dto;

import lombok.*;

@Getter @Setter @ToString @AllArgsConstructor @NoArgsConstructor
public class EmailDto {
    private String to;
    private String subject;
    private String content;
}
