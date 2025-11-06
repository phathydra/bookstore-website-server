package com.tlcn.books.dto;

import lombok.Data;

// DTO này dùng để hứng kết quả từ Aggregation
@Data
public class AnalyticsResult {
    private String _id; // MongoDB $group trả về _id
    private int count;
}