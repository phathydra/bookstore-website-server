package com.tlcn.books.service;

import com.tlcn.books.entity.Discount;
import com.tlcn.books.repository.BookDiscountRepository;
import com.tlcn.books.repository.DiscountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j // Sử dụng Logger thay vì System.out
public class DiscountCleanupScheduler {

    private final DiscountRepository discountRepository;
    private final BookDiscountRepository bookDiscountRepository;

    /**
     * Cron Job: Mặc định chạy 00:00 mỗi đêm.
     * Để test ngay lập tức: Đổi thành fixedRate = 60000 (1 phút chạy 1 lần)
     */
    @Scheduled(cron = "0 0 0 * * ?")
    // @Scheduled(fixedRate = 60000) // <-- Mở dòng này nếu muốn test chạy mỗi phút
    @Transactional
    public void autoRemoveExpiredDiscounts() {
        log.info("----- BẮT ĐẦU QUÉT MÃ GIẢM GIÁ HẾT HẠN -----");

        Date now = new Date();

        // 1. Tìm tất cả các đợt giảm giá đã hết hạn (endDate < now)
        List<Discount> expiredDiscounts = discountRepository.findByEndDateBefore(now);

        if (expiredDiscounts.isEmpty()) {
            log.info("✅ Không có mã giảm giá nào hết hạn tại thời điểm này.");
            return;
        }

        // 2. Lấy danh sách ID
        List<String> expiredDiscountIds = expiredDiscounts.stream()
                .map(Discount::getId)
                .toList(); // Java 16+ dùng .toList() cho gọn

        try {
            // 3. Xóa các liên kết sách trước (BookDiscount)
            // Cần đảm bảo Repository có hàm deleteAllByDiscountIdIn
            bookDiscountRepository.deleteAllByDiscountIdIn(expiredDiscountIds);
            log.info("🗑️ Đã gỡ liên kết sách cho {} mã giảm giá.", expiredDiscountIds.size());

            // 4. Xóa mã giảm giá gốc (Discount)
            discountRepository.deleteAll(expiredDiscounts);
            log.info("🗑️ Đã xóa vĩnh viễn {} mã giảm giá khỏi hệ thống.", expiredDiscounts.size());

        } catch (Exception e) {
            log.error("❌ Lỗi nghiêm trọng khi xóa mã giảm giá: ", e);
            // Transaction sẽ tự rollback nếu có lỗi runtime
            throw e;
        }

        log.info("----- KẾT THÚC QUÉT -----");
    }
}