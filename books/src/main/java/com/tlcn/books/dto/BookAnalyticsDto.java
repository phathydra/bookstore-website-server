package com.tlcn.books.dto;

import lombok.Data;

@Data
public class BookAnalyticsDto {

    private String bookId;

    private long viewCount;

    private long addToCartCount;

    private long purchaseCount;
}
