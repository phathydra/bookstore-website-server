package com.tlcn.books.entity;

// Dùng class này để hứng dữ liệu từ Request Body
public class AnalyticsRequest {
    private String accountId;

    // Cần có getter và setter để Spring Boot hoạt động
    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }
}
