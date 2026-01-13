package com.bookstore.orders.dto;

import lombok.Data;

import java.util.Date;

@Data
public class VoucherDto extends BaseVoucherEntityDto {

    private String id;

    private int userUsageLimit;
    private boolean publish;
}
