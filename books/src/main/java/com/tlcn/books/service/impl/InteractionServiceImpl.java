package com.tlcn.books.service.impl;

import com.tlcn.books.entity.InteractionType;
import com.tlcn.books.entity.UserInteraction;
import com.tlcn.books.repository.UserInteractionRepository;
import com.tlcn.books.service.IInteractionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class InteractionServiceImpl implements IInteractionService {

    private final UserInteractionRepository interactionRepository;
    @Async
    @Override
    public void logSearchInteraction(String accountId, String sessionId, String searchTerm, InteractionType eventType) {
        UserInteraction interaction = new UserInteraction(
                accountId,
                sessionId,
                eventType,
                searchTerm // <-- Dùng constructor mới
        );
        interactionRepository.save(interaction);
    }
    public void logInteraction(String accountId, String sessionId, String bookId, InteractionType eventType) { // <--- SỬA 1

        // Không set timestamp, để @CreatedDate tự làm
        UserInteraction interaction = new UserInteraction(
                accountId, // <--- SỬA 2
                sessionId,
                bookId,
                eventType
        );
        interactionRepository.save(interaction);
    }

    @Async
    @Override
    public void logFilterInteraction(String accountId, String sessionId, String filterData, InteractionType eventType) {
        UserInteraction interaction = new UserInteraction(
                accountId,
                sessionId,
                eventType,
                filterData, // <-- Dữ liệu JSON của filter
                true // <-- Chỉ để gọi đúng constructor
        );
        interactionRepository.save(interaction);
    }

    @Async
    @Override
    public void logPlaceOrderAttempt(String accountId, String sessionId, String orderData, InteractionType eventType) {
        UserInteraction interaction = new UserInteraction(
                accountId,
                sessionId,
                eventType,
                orderData, // <-- Dùng constructor mới
                1 // Số int này chỉ để phân biệt constructor
        );
        interactionRepository.save(interaction);
    }

    @Override
    public List<String> getRecentViewedBookIds(String accountId, int limit) {
        // 1. Tạo một đối tượng Pageable để lấy 50 sự kiện VIEW gần nhất
        // (Chúng ta lấy nhiều hơn 'limit' vì có thể user xem 1 cuốn sách nhiều lần)
        Pageable pageable = PageRequest.of(0, 50);

        // 2. Gọi Repository để truy vấn CSDL
        List<UserInteraction> recentInteractions =
                interactionRepository.findByAccountIdAndEventTypeOrderByTimestampDesc(
                        accountId,
                        InteractionType.VIEW,
                        pageable
                );

        // 3. Dùng LinkedHashSet để giữ thứ tự và loại bỏ trùng lặp
        // (LinkedHashSet sẽ giữ bookId của lần xem MỚI NHẤT)
        Set<String> distinctBookIds = new LinkedHashSet<>();

        for (UserInteraction interaction : recentInteractions) {
            // Đảm bảo bookId không rỗng
            if (interaction.getBookId() != null && !interaction.getBookId().isEmpty()) {
                distinctBookIds.add(interaction.getBookId());
            }

            // 4. Dừng lại khi đã đủ số lượng 'limit'
            if (distinctBookIds.size() >= limit) {
                break;
            }
        }

        // 5. Trả về một List<String>
        return new ArrayList<>(distinctBookIds);
    }
}