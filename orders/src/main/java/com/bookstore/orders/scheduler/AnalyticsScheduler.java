// package com.bookstore.orders.scheduler;
package com.bookstore.orders.scheduler; // Ghi chú: Tạo package mới nếu cần

import com.bookstore.orders.dto.BestSellingBookDto;
import com.bookstore.orders.service.IOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class AnalyticsScheduler {

    @Autowired
    private IOrderService orderService; // Service chứa hàm thống kê

    @Autowired
    private RestTemplate restTemplate;

    @Value("${book-service.base-url}") // Lấy URL từ file properties
    private String bookServiceBaseUrl;

    /**
     * Chạy tự động vào 3h sáng mỗi ngày
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void analyzeAndUpdaeBookTags() {
        System.out.println("Bắt đầu Tác vụ tự động: Phân tích và cập nhật Tags sách...");

        // 1. Lấy sách bán chậm (ví dụ: 100 cuốn bán chậm nhất)
        Page<BestSellingBookDto> worstSellers = orderService.getWorstSellingBooksPaginated(null, null, 0, 100);
        List<String> coldBookIds = worstSellers.getContent().stream()
                .map(BestSellingBookDto::getBookId)
                .toList();

        // 2. Lấy sách bán chạy đều (ví dụ: bán > 10 cuốn/tháng trong 3 tháng qua)
        Page<BestSellingBookDto> consistentSellers = orderService.getConsistentSellersPaginated(3, 10, 0, 100);
        List<String> hotBookIds = consistentSellers.getContent().stream()
                .map(BestSellingBookDto::getBookId)
                .toList();

        // 3. Gói dữ liệu
        Map<String, List<String>> tagUpdates = new HashMap<>();
        tagUpdates.put("COLD_SELLER", coldBookIds);
        tagUpdates.put("HOT_SELLER", hotBookIds); // Bạn có thể đổi tên tag nếu muốn

        // 4. Gọi sang Book Service để cập nhật
        String url = bookServiceBaseUrl + "/api/book/internal/update-tags";
        try {
            restTemplate.postForEntity(url, tagUpdates, Void.class);
            System.out.println("Cập nhật Tags thành công!");
        } catch (Exception e) {
            System.err.println("Lỗi khi gọi Book Service để cập nhật tags: " + e.getMessage());
        }
    }
}