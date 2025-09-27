// src/main/java/com/bookstore/orders/dto/BestSellingBookDto.java
package com.bookstore.orders.dto;

public class BestSellingBookDto {
    private String bookId;
    private String bookName;
    private int totalSold;

    // THÊM TRƯỜNG MỚI
    private int stockQuantity;
    private String stockStatus; // Ví dụ: "Sắp hết hàng", "Bình thường"

    public BestSellingBookDto() {
    }

    public BestSellingBookDto(String bookId, String bookName, int totalSold) {
        this.bookId = bookId;
        this.bookName = bookName;
        this.totalSold = totalSold;
    }

    public String getBookId() {
        return bookId;
    }

    public void setBookId(String bookId) {
        this.bookId = bookId;
    }

    public String getBookName() {
        return bookName;
    }

    public void setBookName(String bookName) {
        this.bookName = bookName;
    }

    public int getTotalSold() {
        return totalSold;
    }

    public void setTotalSold(int totalSold) {
        this.totalSold = totalSold;
    }

    // GETTERS VÀ SETTERS MỚI
    public int getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(int stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public String getStockStatus() {
        return stockStatus;
    }

    public void setStockStatus(String stockStatus) {
        this.stockStatus = stockStatus;
    }
}