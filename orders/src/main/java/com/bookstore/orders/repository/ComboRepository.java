package com.bookstore.orders.repository;

import com.bookstore.orders.entity.Combo;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ComboRepository extends MongoRepository<Combo, String> {

    // Tìm các combo còn hoạt động và trong thời gian áp dụng
    List<Combo> findByIsActiveTrueAndStartDateBeforeAndEndDateAfter(
            LocalDateTime now1, LocalDateTime now2
    );

    // Tìm các combo hoạt động không có ngày
    List<Combo> findByIsActiveTrueAndStartDateIsNullAndEndDateIsNull(boolean isActive);

    // *** THÊM METHOD MỚI NÀY ***
    // Tìm các combo đang hoạt động, còn hạn, VÀ chứa một bookId cụ thể
    List<Combo> findByIsActiveTrueAndStartDateBeforeAndEndDateAfterAndBookIdsContaining(
            LocalDateTime now1, LocalDateTime now2, String bookId
    );
    // (Bạn có thể thêm 1 method tương tự cho trường hợp không có ngày bắt đầu/kết thúc)

    // Hàm mới cho Scheduled Task
    List<Combo> findByIsActiveFalseAndStartDateBeforeAndEndDateAfter(LocalDateTime startDate, LocalDateTime endDate);

    // Hàm mới cho Scheduled Task
    List<Combo> findByIsActiveTrueAndEndDateBefore(LocalDateTime endDate);
    List<Combo> findByIsActiveTrue();
}