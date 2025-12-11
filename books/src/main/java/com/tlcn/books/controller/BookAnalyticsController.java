package com.tlcn.books.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tlcn.books.dto.BookAnalyticsDto;
import com.tlcn.books.dto.FilterTrackRequest;
import com.tlcn.books.dto.PlaceOrderTrackRequest;
import com.tlcn.books.dto.SearchTrackRequest;
import com.tlcn.books.entity.AnalyticsRequest;
import com.tlcn.books.entity.InteractionType;
import com.tlcn.books.service.IBookAnalyticsService; // Service để CẬP NHẬT
import com.tlcn.books.service.IInteractionService; // Service để LOG
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

    // Service để ĐỌC và CẬP NHẬT BẢNG TỔNG HỢP (book_analytics)
    private final IBookAnalyticsService analyticsService;

    // Service để GHI LOG CHI TIẾT (interactions)
    private final IInteractionService interactionService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // API Đọc (Get) giữ nguyên
    @GetMapping("/{bookId}")
    public BookAnalyticsDto getAnalytics(@PathVariable String bookId) {
        return analyticsService.getAnalyticsByBookId(bookId);
    }

    // API Ghi (Post) SỬA LẠI
    @PostMapping("/{bookId}/view")
    public ResponseEntity<Void> logView(
            @PathVariable String bookId,
            @RequestHeader(value = "X-Session-ID", required = false) String sessionId,
            Principal principal
    ) {
        // SỬA: Đổi tên 'userId' thành 'accountId' cho thống nhất
        String accountId = (principal != null) ? principal.getName() : null;

        // TÁCH NHIỆM VỤ RA

        // 1. (Code mới) Ghi log chi tiết (nhanh, async, fire-and-forget)
        // Dùng cho recommender, xử lý sau
        interactionService.logInteraction(accountId, sessionId, bookId, InteractionType.VIEW);

        // 2. (Code cũ) Vẫn giữ "hàm +1" trực tiếp (synchronous)
        // Dùng để cập nhật UI ngay lập tức
        analyticsService.incrementView(bookId);

        return ResponseEntity.ok().build(); // Trả về 200 OK
    }

    @PostMapping("/{bookId}/add-to-cart")
    public ResponseEntity<Void> logAddToCart(
            @PathVariable String bookId,
            @RequestHeader(value = "X-Session-ID", required = false) String sessionId,
            @RequestBody AnalyticsRequest request // <--- (1) DÙNG @RequestBody ĐỂ ĐỌC BODY
    ) {

        // (2) LẤY accountId TỪ OBJECT REQUEST
        String accountId = (request != null) ? request.getAccountId() : null;

        // 3. Ghi log
        // (Code này đã đúng từ file service của bạn)
        interactionService.logInteraction(accountId, sessionId, bookId, InteractionType.ADD_TO_CART);

        // 4. Vẫn giữ "hàm +1"
        analyticsService.incrementAddToCart(bookId);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/track/search")
    public ResponseEntity<Void> logSearch(
            @RequestBody SearchTrackRequest request, // <-- Nhận DTO từ body
            @RequestHeader(value = "X-Session-ID", required = false) String sessionId
    ) {

        // Gọi service mới để lưu
        interactionService.logSearchInteraction(
                request.getAccountId(),
                sessionId,
                request.getSearchTerm(), // <-- Lấy từ khóa
                InteractionType.SEARCH   // <-- Ghi là sự kiện SEARCH
        );

        return ResponseEntity.ok().build();
    }

    @PostMapping("/track/filter")
    public ResponseEntity<Void> logFilter(
            @RequestBody FilterTrackRequest request, // <-- Nhận DTO từ body
            @RequestHeader(value = "X-Session-ID", required = false) String sessionId
    ) {

        // Gọi service mới để lưu
        interactionService.logFilterInteraction(
                request.getAccountId(),
                sessionId,
                request.getFilterData(), // <-- Lấy chuỗi JSON filter
                InteractionType.FILTER   // <-- Ghi là sự kiện FILTER
        );

        return ResponseEntity.ok().build();
    }

    @PostMapping("/{bookId}/click-summary")
    public ResponseEntity<Void> logClickSummary(
            @PathVariable String bookId,
            @RequestHeader(value = "X-Session-ID", required = false) String sessionId,
            @RequestBody AnalyticsRequest request // Tái sử dụng DTO này
    ) {
        String accountId = (request != null) ? request.getAccountId() : null;

        // Chỉ cần ghi log chi tiết, không cần +1 vào bảng analytics chính
        interactionService.logInteraction(
                accountId,
                sessionId,
                bookId,
                InteractionType.CLICK_SUMMARY
        );

        return ResponseEntity.ok().build();
    }

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
            System.err.println("Không thể serialize PlaceOrderTrackRequest (attempt): " + e.getMessage());
        }

        return ResponseEntity.ok().build();
    }

    // 🆕 Thêm endpoint mới cho ORDER_SUCCESS
    @PostMapping("/track/order-success")
    public ResponseEntity<Void> logOrderSuccess(
            @RequestBody PlaceOrderTrackRequest request,
            @RequestHeader(value = "X-Session-ID", required = false) String sessionId
    ) {
        try {
            String orderDataAsJson = objectMapper.writeValueAsString(request);

            interactionService.logPlaceOrderAttempt(
                    request.getAccountId(),
                    sessionId,
                    orderDataAsJson,
                    InteractionType.ORDER_SUCCESS // 🧩 Enum mới
            );
        } catch (JsonProcessingException e) {
            System.err.println("Không thể serialize PlaceOrderTrackRequest (success): " + e.getMessage());
        }

        return ResponseEntity.ok().build();
    }

    @GetMapping("/recent-views")
    public ResponseEntity<List<String>> getRecentViews(
            Principal principal,
            @RequestParam(defaultValue = "5") int limit
    ) {
        // Luôn kiểm tra principal để đảm bảo an toàn
        if (principal == null || principal.getName() == null) {
            // Trả về 401 Unauthorized nếu không có user
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Collections.emptyList());
        }

        String accountId = principal.getName();

        // Gọi service (bạn sẽ cần tạo phương thức này ở Bước 2)
        List<String> bookIds = interactionService.getRecentViewedBookIds(accountId, limit);

        return ResponseEntity.ok(bookIds);
    }
}