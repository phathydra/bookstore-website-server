package com.tlcn.books.dto;

import lombok.Data;

import java.util.Date;

@Data
public class DiscountDto {
    String id;
    int percentage;

    Date startDate;
    Date endDate;

    String type;
}
