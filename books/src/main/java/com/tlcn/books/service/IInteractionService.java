package com.tlcn.books.service;

import com.tlcn.books.entity.InteractionType;

public interface IInteractionService {
    // Sửa 'String userId' thành 'String accountId' ở đây
    void logInteraction(String accountId, String sessionId, String bookId, InteractionType eventType);
    void logSearchInteraction(String accountId, String sessionId, String searchTerm, InteractionType eventType);
    void logFilterInteraction(String accountId, String sessionId, String filterData, InteractionType eventType);
    void logPlaceOrderAttempt(String accountId, String sessionId, String orderData, InteractionType eventType);
}