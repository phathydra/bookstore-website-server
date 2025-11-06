package com.tlcn.books.processor; // <-- Sửa lại package cho chuẩn

import com.tlcn.books.dto.AnalyticsResult;
import com.tlcn.books.entity.BookAnalytics;
import com.tlcn.books.entity.InteractionType;
import com.tlcn.books.repository.BookAnalyticsRepository;
import com.tlcn.books.repository.UserInteractionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j // Dùng Log thay vì System.out
public class AnalyticsProcessor {

    private final UserInteractionRepository interactionRepository;
    private final BookAnalyticsRepository bookAnalyticsRepository;
    // Bỏ BookAnalyticsMapper vì không cần nữa

    // Chạy vào 2 giờ sáng mỗi ngày
    @Scheduled(cron = "0 0 2 * * ?")
    public void processDailyAnalytics() {
        log.info("Bắt đầu xử lý Analytics hàng ngày...");

        // Đặt mốc thời gian là 24 giờ trước
        LocalDateTime processTime = LocalDateTime.now();
        LocalDateTime twentyFourHoursAgo = processTime.minusDays(1);

        // 1. Xử lý VIEW
        log.info("Đang xử lý VIEW events...");
        List<AnalyticsResult> viewCounts = interactionRepository
                .countEventsByTypeAfter(InteractionType.VIEW, twentyFourHoursAgo);

        viewCounts.forEach(result -> {
            BookAnalytics analytics = bookAnalyticsRepository.findByBookId(result.get_id())
                    .orElse(new BookAnalytics(null, result.get_id(), 0, 0, 0));
            analytics.setViewCount(analytics.getViewCount() + result.getCount());
            bookAnalyticsRepository.save(analytics);
        });

        // 2. Xử lý ADD_TO_CART
        log.info("Đang xử lý ADD_TO_CART events...");
        List<AnalyticsResult> cartCounts = interactionRepository
                .countEventsByTypeAfter(InteractionType.ADD_TO_CART, twentyFourHoursAgo);

        cartCounts.forEach(result -> {
            BookAnalytics analytics = bookAnalyticsRepository.findByBookId(result.get_id())
                    .orElse(new BookAnalytics(null, result.get_id(), 0, 0, 0));
            analytics.setAddToCartCount(analytics.getAddToCartCount() + result.getCount());
            bookAnalyticsRepository.save(analytics);
        });

        // 3. Xử lý PURCHASE
        log.info("Đang xử lý PURCHASE events...");
        List<AnalyticsResult> purchaseCounts = interactionRepository
                .countEventsByTypeAfter(InteractionType.PURCHASE, twentyFourHoursAgo);

        purchaseCounts.forEach(result -> {
            BookAnalytics analytics = bookAnalyticsRepository.findByBookId(result.get_id())
                    .orElse(new BookAnalytics(null, result.get_id(), 0, 0, 0));
            analytics.setPurchaseCount(analytics.getPurchaseCount() + result.getCount());
            bookAnalyticsRepository.save(analytics);
        });

        // 4. Xóa log đã xử lý (xóa tất cả log *trước* thời điểm bắt đầu xử lý)
        log.info("Đang xóa log đã xử lý...");
        interactionRepository.deleteByEventTypeAndTimestampBefore(InteractionType.VIEW, processTime);
        interactionRepository.deleteByEventTypeAndTimestampBefore(InteractionType.ADD_TO_CART, processTime);
        interactionRepository.deleteByEventTypeAndTimestampBefore(InteractionType.PURCHASE, processTime);

        log.info("Xử lý Analytics hoàn tất.");
    }
}