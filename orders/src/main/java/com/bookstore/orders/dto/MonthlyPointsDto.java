package com.bookstore.orders.dto;

import lombok.Data;

@Data
public class MonthlyPointsDto {
    private String id;

    private int year;

    private int month;

    private Double point;
}
