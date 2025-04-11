package com.tlcn.books.controller;

import com.tlcn.books.dto.BookAnalyticsDto;
import com.tlcn.books.service.IBookAnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class BookAnalyticsController {

    private final IBookAnalyticsService analyticsService;

    @GetMapping("/{bookId}")
    public BookAnalyticsDto getAnalytics(@PathVariable String bookId) {
        return analyticsService.getAnalyticsByBookId(bookId);
    }

    @PostMapping("/{bookId}/view")
    public BookAnalyticsDto incrementView(@PathVariable String bookId) {
        return analyticsService.incrementView(bookId);
    }

    @PostMapping("/{bookId}/add-to-cart")
    public BookAnalyticsDto incrementAddToCart(@PathVariable String bookId) {
        return analyticsService.incrementAddToCart(bookId);
    }

    @PostMapping("/{bookId}/purchase")
    public BookAnalyticsDto incrementPurchase(@PathVariable String bookId) {
        return analyticsService.incrementPurchase(bookId);
    }
}
