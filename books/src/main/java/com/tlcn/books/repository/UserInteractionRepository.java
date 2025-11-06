package com.tlcn.books.repository;

import com.tlcn.books.dto.AnalyticsResult;
import com.tlcn.books.entity.InteractionType;
import com.tlcn.books.entity.UserInteraction;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface UserInteractionRepository extends MongoRepository<UserInteraction, String> {

    /**
     * Dùng Aggregation Pipeline để đếm số lượng sự kiện
     * @param eventType Loại sự kiện (VIEW, ADD_TO_CART, ...)
     * @param timestamp Mốc thời gian (ví dụ: 24h qua)
     * @return Danh sách các { bookId, totalCount }
     */
    @Aggregation(pipeline = {
            "{ $match: { eventType: ?0, timestamp: { $gte: ?1 } } }", // Lọc theo loại và thời gian
            "{ $group: { _id: '$bookId', count: { $sum: 1 } } }"      // Nhóm theo bookId và đếm
    })
    List<AnalyticsResult> countEventsByTypeAfter(InteractionType eventType, LocalDateTime timestamp);

    /**
     * Xóa các sự kiện đã được xử lý (để tránh đếm lại)
     */
    void deleteByEventTypeAndTimestampBefore(InteractionType eventType, LocalDateTime timestamp);
}