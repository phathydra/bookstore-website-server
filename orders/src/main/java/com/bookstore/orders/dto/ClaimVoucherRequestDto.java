package com.bookstore.orders.dto;

import lombok.Data;

@Data
public class ClaimVoucherRequestDto {
    private String accountId;

    private ObtainableVoucherDto obtainableVoucherDto;
}
