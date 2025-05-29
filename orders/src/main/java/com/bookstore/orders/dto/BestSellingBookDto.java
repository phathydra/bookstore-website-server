// src/main/java/com/bookstore/orders/dto/BestSellingBookDto.java
package com.bookstore.orders.dto;

public class BestSellingBookDto {
    private String bookId;
    private String bookName;
    private int totalSold;

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
}
