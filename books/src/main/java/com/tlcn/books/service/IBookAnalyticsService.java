package com.tlcn.books.service;

import com.tlcn.books.dto.BookAnalyticsDto;

public interface IBookAnalyticsService {

    BookAnalyticsDto getAnalyticsByBookId(String bookId);

    BookAnalyticsDto incrementView(String bookId);

    BookAnalyticsDto incrementAddToCart(String bookId);

    BookAnalyticsDto incrementPurchase(String bookId);
}
