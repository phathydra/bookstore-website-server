package com.bookstore.orders.service.impl;

import com.bookstore.orders.entity.Combo;
import com.bookstore.orders.repository.ComboRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ComboStatusUpdaterService {

    private static final Logger logger = LoggerFactory.getLogger(ComboStatusUpdaterService.class);
    private final ComboRepository comboRepository;

    @Autowired
    public ComboStatusUpdaterService(ComboRepository comboRepository) {
        this.comboRepository = comboRepository;
    }

    @Scheduled(cron = "0 */5 * * * *")
    public void updateComboStatuses() {
        LocalDateTime now = LocalDateTime.now();
        logger.info("Chạy tác vụ cập nhật trạng thái combo lúc: {}", now);

        // 1. Tìm các combo cần được KÍCH HOẠT
        // (Chưa active, ngày bắt đầu đã qua, ngày kết thúc chưa tới)
        List<Combo> combosToActivate = comboRepository.findByIsActiveFalseAndStartDateBeforeAndEndDateAfter(now, now);

        for (Combo combo : combosToActivate) {
            combo.setActive(true);
            comboRepository.save(combo);
            logger.info("Đã KÍCH HOẠT combo: {}", combo.getComboId());
        }

        // 2. Tìm các combo cần HẾT HẠN
        // (Đang active, ngày kết thúc đã qua)
        List<Combo> combosToExpire = comboRepository.findByIsActiveTrueAndEndDateBefore(now);

        for (Combo combo : combosToExpire) {
            combo.setActive(false);
            comboRepository.save(combo);
            logger.info("Đã HẾT HẠN combo: {}", combo.getComboId());
        }
    }
}