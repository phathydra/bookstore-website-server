package com.tlcn.books.mapper;

import com.tlcn.books.dto.BookAnalyticsDto;
import com.tlcn.books.entity.BookAnalytics;
import org.springframework.stereotype.Component;

@Component
public class BookAnalyticsMapper {

    public BookAnalyticsDto toDto(BookAnalytics entity) {
        BookAnalyticsDto dto = new BookAnalyticsDto();
        dto.setBookId(entity.getBookId());
        dto.setViewCount(entity.getViewCount());
        dto.setAddToCartCount(entity.getAddToCartCount());
        dto.setPurchaseCount(entity.getPurchaseCount());
        return dto;
    }

    public BookAnalytics toEntity(BookAnalyticsDto dto) {
        BookAnalytics entity = new BookAnalytics();
        entity.setBookId(dto.getBookId());
        entity.setViewCount(dto.getViewCount());
        entity.setAddToCartCount(dto.getAddToCartCount());
        entity.setPurchaseCount(dto.getPurchaseCount());
        return entity;
    }
}
