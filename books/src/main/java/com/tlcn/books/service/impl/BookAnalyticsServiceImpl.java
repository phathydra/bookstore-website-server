package com.tlcn.books.service.impl;

import com.tlcn.books.dto.BookAnalyticsDto;
import com.tlcn.books.entity.BookAnalytics;
import com.tlcn.books.mapper.BookAnalyticsMapper;
import com.tlcn.books.repository.BookAnalyticsRepository;
import com.tlcn.books.service.IBookAnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BookAnalyticsServiceImpl implements IBookAnalyticsService {

    private final BookAnalyticsRepository bookAnalyticsRepository;
    private final BookAnalyticsMapper bookAnalyticsMapper;

    @Override
    public BookAnalyticsDto getAnalyticsByBookId(String bookId) {
        BookAnalytics analytics = bookAnalyticsRepository.findByBookId(bookId)
                .orElse(new BookAnalytics(null, bookId, 0, 0, 0));
        return bookAnalyticsMapper.toDto(analytics);
    }

    @Override
    public BookAnalyticsDto incrementView(String bookId) {
        BookAnalytics analytics = bookAnalyticsRepository.findByBookId(bookId)
                .orElse(new BookAnalytics(null, bookId, 0, 0, 0));
        analytics.setViewCount(analytics.getViewCount() + 1);
        return bookAnalyticsMapper.toDto(bookAnalyticsRepository.save(analytics));
    }

    @Override
    public BookAnalyticsDto incrementAddToCart(String bookId) {
        BookAnalytics analytics = bookAnalyticsRepository.findByBookId(bookId)
                .orElse(new BookAnalytics(null, bookId, 0, 0, 0));
        analytics.setAddToCartCount(analytics.getAddToCartCount() + 1);
        return bookAnalyticsMapper.toDto(bookAnalyticsRepository.save(analytics));
    }

    @Override
    public BookAnalyticsDto incrementPurchase(String bookId) {
        BookAnalytics analytics = bookAnalyticsRepository.findByBookId(bookId)
                .orElse(new BookAnalytics(null, bookId, 0, 0, 0));
        analytics.setPurchaseCount(analytics.getPurchaseCount() + 1);
        return bookAnalyticsMapper.toDto(bookAnalyticsRepository.save(analytics));
    }
}
