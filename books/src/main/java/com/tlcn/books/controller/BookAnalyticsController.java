package com.tlcn.books.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tlcn.books.dto.*;
import com.tlcn.books.entity.AnalyticsRequest;
import com.tlcn.books.entity.InteractionType;
import com.tlcn.books.service.IBookAnalyticsService;
import com.tlcn.books.service.IInteractionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Collections;
import java.util.List;

@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class BookAnalyticsController {

    private final IBookAnalyticsService analyticsService;
    private final IInteractionService interactionService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // 1. API Lấy thống kê
    @GetMapping("/{bookId}")
    public BookAnalyticsDto getAnalytics(@PathVariable String bookId) {
        return analyticsService.getAnalyticsByBookId(bookId);
    }

    // 2. API Log View (Đã giữ nguyên logic view bạn muốn)
    @PostMapping("/{bookId}/view")
    public ResponseEntity<Void> logView(
            @PathVariable String bookId,
            @RequestHeader(value = "X-Session-ID", required = false) String sessionId,
            Principal principal
    ) {
        String accountId = (principal != null) ? principal.getName() : null;

        // Log cho AI
        interactionService.logInteraction(accountId, sessionId, bookId, InteractionType.VIEW);
        // Cộng view hiển thị ngay
        analyticsService.incrementView(bookId);

        return ResponseEntity.ok().build();
    }

    // 3. API Add to Cart
    @PostMapping("/{bookId}/add-to-cart")
    public ResponseEntity<Void> logAddToCart(
            @PathVariable String bookId,
            @RequestHeader(value = "X-Session-ID", required = false) String sessionId,
            @RequestBody AnalyticsRequest request
    ) {
        String accountId = (request != null) ? request.getAccountId() : null;

        interactionService.logInteraction(accountId, sessionId, bookId, InteractionType.ADD_TO_CART);
        analyticsService.incrementAddToCart(bookId);

        return ResponseEntity.ok().build();
    }

    // 4. API Search
    @PostMapping("/track/search")
    public ResponseEntity<Void> logSearch(
            @RequestBody SearchTrackRequest request,
            @RequestHeader(value = "X-Session-ID", required = false) String sessionId
    ) {
        interactionService.logSearchInteraction(
                request.getAccountId(),
                sessionId,
                request.getSearchTerm(),
                InteractionType.SEARCH
        );
        return ResponseEntity.ok().build();
    }

    // 5. API Filter
    @PostMapping("/track/filter")
    public ResponseEntity<Void> logFilter(
            @RequestBody FilterTrackRequest request,
            @RequestHeader(value = "X-Session-ID", required = false) String sessionId
    ) {
        interactionService.logFilterInteraction(
                request.getAccountId(),
                sessionId,
                request.getFilterData(),
                InteractionType.FILTER
        );
        return ResponseEntity.ok().build();
    }

    // 6. API Click Summary
    @PostMapping("/{bookId}/click-summary")
    public ResponseEntity<Void> logClickSummary(
            @PathVariable String bookId,
            @RequestHeader(value = "X-Session-ID", required = false) String sessionId,
            @RequestBody AnalyticsRequest request
    ) {
        String accountId = (request != null) ? request.getAccountId() : null;
        interactionService.logInteraction(
                accountId,
                sessionId,
                bookId,
                InteractionType.CLICK_SUMMARY
        );
        return ResponseEntity.ok().build();
    }

    // 7. API Place Order Attempt (Mới nhấn nút đặt hàng, chưa chắc thành công)
    @PostMapping("/track/place-order")
    public ResponseEntity<Void> logPlaceOrderAttempt(
            @RequestBody PlaceOrderTrackRequest request,
            @RequestHeader(value = "X-Session-ID", required = false) String sessionId
    ) {
        try {
            String orderDataAsJson = objectMapper.writeValueAsString(request);
            interactionService.logPlaceOrderAttempt(
                    request.getAccountId(),
                    sessionId,
                    orderDataAsJson,
                    InteractionType.PLACE_ORDER_ATTEMPT
            );
        } catch (JsonProcessingException e) {
            System.err.println("Lỗi JSON PlaceOrder Attempt: " + e.getMessage());
        }
        return ResponseEntity.ok().build();
    }

    // 8. API ORDER SUCCESS (QUAN TRỌNG: Đã sửa logic)
    @PostMapping("/track/order-success")
    public ResponseEntity<Void> logOrderSuccess(
            @RequestBody PlaceOrderTrackRequest request,
            @RequestHeader(value = "X-Session-ID", required = false) String sessionId
    ) {
        try {
            // A. Log tương tác cho AI (Lưu nguyên cục JSON đơn hàng)
            String orderDataAsJson = objectMapper.writeValueAsString(request);
            interactionService.logPlaceOrderAttempt(
                    request.getAccountId(),
                    sessionId,
                    orderDataAsJson,
                    InteractionType.ORDER_SUCCESS
            );

            // B. CẬP NHẬT SỐ LƯỢNG ĐÃ BÁN (LOGIC MỚI)
            if (request.getItems() != null && !request.getItems().isEmpty()) {
                for (OrderItemTrackDto item : request.getItems()) {
                    // item.getBookId(): ID sách
                    // item.getQuantity(): Số lượng khách mua (VD: 5 cuốn)

                    // Gọi hàm service mới để cộng dồn
                    analyticsService.incrementPurchase(item.getBookId(), item.getQuantity());
                }
            }

        } catch (JsonProcessingException e) {
            System.err.println("Lỗi JSON Order Success: " + e.getMessage());
        }

        return ResponseEntity.ok().build();
    }

    // 9. API Lấy sách xem gần đây
    @GetMapping("/recent-views")
    public ResponseEntity<List<String>> getRecentViews(
            Principal principal,
            @RequestParam(defaultValue = "5") int limit
    ) {
        if (principal == null || principal.getName() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Collections.emptyList());
        }
        String accountId = principal.getName();
        List<String> bookIds = interactionService.getRecentViewedBookIds(accountId, limit);
        return ResponseEntity.ok(bookIds);
    }
}