package com.bookstore.accounts.constants;

public class AccountConstants {
    private AccountConstants(){

    }

    public static final String STATUS_201 = "201";
    public static final String MESSAGE_201 = "Account created successfully";

    public static final String STATUS_200 = "200";
    public static final String MESSAGE_200 = "Request processed successfully";

    public static final String STATUS_400 = "400";
    public static final String MESSAGE_400_EMAIL_EXISTS = "Email already exists";  // Thêm thông báo lỗi email đã tồn tại

    public static final String STATUS_500 = "500";
    public static final String MESSAGE_500 = "Error";

    public static final String STATUS_404 = "404";
    public static final String MESSAGE_404_ACCOUNT_NOT_FOUND = "Account not found";
}
