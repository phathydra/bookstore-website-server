package com.tlcn.books.service.impl;

import com.tlcn.books.entity.InteractionType;
import com.tlcn.books.entity.UserInteraction;
import com.tlcn.books.repository.UserInteractionRepository;
import com.tlcn.books.service.IInteractionService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

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
}