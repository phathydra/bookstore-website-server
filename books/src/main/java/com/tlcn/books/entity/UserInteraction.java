package com.tlcn.books.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;


@Document(collection = "user_interactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserInteraction {
    @Id
    private String id;
    private String accountId;
    private String sessionId;
    private String bookId;
    private InteractionType eventType;

    private String searchTerm;
    private String filterData;
    private String orderData;

    @CreatedDate
    private LocalDateTime timestamp;

    // Constructor cũ (cho VIEW, ADD_TO_CART, v.v...)
    public UserInteraction(String accountId, String sessionId, String bookId, InteractionType eventType) {
        this.accountId = accountId;
        this.sessionId = sessionId;
        this.bookId = bookId;
        this.eventType = eventType;
    }

    // (THÊM 2) Constructor mới (cho SEARCH)
    public UserInteraction(String accountId, String sessionId, InteractionType eventType, String searchTerm) {
        this.accountId = accountId;
        this.sessionId = sessionId;
        this.eventType = eventType;
        this.searchTerm = searchTerm;
        // bookId sẽ là null, hoàn toàn ổn
    }
    public UserInteraction(String accountId, String sessionId, InteractionType eventType, String filterData, boolean isFilter) {
        this.accountId = accountId;
        this.sessionId = sessionId;
        this.eventType = eventType;
        this.filterData = filterData; // <-- Lưu dữ liệu filter
        // boolean isFilter chỉ để phân biệt constructor, không cần lưu
    }

    public UserInteraction(String accountId, String sessionId, InteractionType eventType, String orderData, int type) {
        this.accountId = accountId;
        this.sessionId = sessionId;
        this.eventType = eventType;
        this.orderData = orderData; // <-- Lưu data
    }
}