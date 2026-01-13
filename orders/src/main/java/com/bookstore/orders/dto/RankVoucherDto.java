package com.bookstore.orders.dto;

import lombok.Data;

import java.util.Date;

@Data
public class RankVoucherDto extends BaseVoucherEntityDto {

    private String id;
    private int rank;
}
