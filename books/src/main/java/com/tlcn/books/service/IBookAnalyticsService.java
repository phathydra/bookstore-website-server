package com.tlcn.books.service;

import com.tlcn.books.dto.BookAnalyticsDto;

public interface IBookAnalyticsService {

    BookAnalyticsDto getAnalyticsByBookId(String bookId);

    BookAnalyticsDto incrementView(String bookId);

    BookAnalyticsDto incrementAddToCart(String bookId);

    // SỬA: Thêm tham số 'quantity' để cộng đúng số lượng
    BookAnalyticsDto incrementPurchase(String bookId, int quantity);

}