package com.bookstore.orders.service.impl;

import com.bookstore.orders.dto.ComboDto;
import com.bookstore.orders.entity.Combo;
import com.bookstore.orders.repository.ComboRepository;
import com.bookstore.orders.service.IComboService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ComboServiceImpl implements IComboService {

    private final ComboRepository comboRepository;

    @Autowired
    public ComboServiceImpl(ComboRepository comboRepository) {
        this.comboRepository = comboRepository;
    }

    @Override
    public void createCombo(ComboDto comboDto) {
        Combo combo = new Combo();
        combo.setName(comboDto.getName());
        combo.setDescription(comboDto.getDescription());
        combo.setBookIds(comboDto.getBookIds());
        combo.setDiscountType(comboDto.getDiscountType());
        combo.setDiscountValue(comboDto.getDiscountValue());

        // Xử lý ngày bắt đầu
        if (comboDto.getStartDate() != null && !comboDto.getStartDate().isEmpty()) {
            LocalDate startDate = LocalDate.parse(comboDto.getStartDate());
            combo.setStartDate(startDate.atStartOfDay());
        }

        // Xử lý ngày kết thúc
        if (comboDto.getEndDate() != null && !comboDto.getEndDate().isEmpty()) {
            LocalDate endDate = LocalDate.parse(comboDto.getEndDate());
            combo.setEndDate(endDate.atTime(23, 59, 59));
        }

        // Tính toán trạng thái active dựa trên ngày
        LocalDateTime now = LocalDateTime.now();
        if (combo.getStartDate() != null && combo.getEndDate() != null) {
            combo.setActive(!now.isBefore(combo.getStartDate()) && !now.isAfter(combo.getEndDate()));
        } else {
            combo.setActive(false); // Nếu không set ngày, mặc định là false
        }

        comboRepository.save(combo);
    }

    @Override
    public Page<Combo> getAllCombos(Pageable pageable) {
        // (Đây là phiên bản đã tối ưu, không check update ở đây nữa)
        return comboRepository.findAll(pageable);
    }

    @Override
    public void deleteCombo(String comboId) {
        if (!comboRepository.existsById(comboId)) {
            throw new RuntimeException("Không tìm thấy combo với ID: " + comboId);
        }
        comboRepository.deleteById(comboId);
    }


    @Override
    public Combo getComboById(String comboId) {
        return comboRepository.findById(comboId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy combo với ID: " + comboId));
    }

    @Override
    public Combo updateCombo(String comboId, ComboDto comboDto) {
        // 1. Lấy combo hiện có
        Combo existingCombo = getComboById(comboId); // Tái sử dụng hàm getById để kiểm tra tồn tại

        // 2. Cập nhật các trường
        existingCombo.setName(comboDto.getName());
        existingCombo.setDescription(comboDto.getDescription());
        existingCombo.setBookIds(comboDto.getBookIds());
        existingCombo.setDiscountType(comboDto.getDiscountType());
        existingCombo.setDiscountValue(comboDto.getDiscountValue());

        // 3. Cập nhật ngày (cho phép xóa ngày bằng cách gửi rỗng/null)
        if (comboDto.getStartDate() != null && !comboDto.getStartDate().isEmpty()) {
            LocalDate startDate = LocalDate.parse(comboDto.getStartDate());
            existingCombo.setStartDate(startDate.atStartOfDay());
        } else {
            existingCombo.setStartDate(null); // Cho phép xóa ngày
        }

        if (comboDto.getEndDate() != null && !comboDto.getEndDate().isEmpty()) {
            LocalDate endDate = LocalDate.parse(comboDto.getEndDate());
            existingCombo.setEndDate(endDate.atTime(23, 59, 59));
        } else {
            existingCombo.setEndDate(null); // Cho phép xóa ngày
        }

        // 4. Tính toán lại trạng thái Active
        LocalDateTime now = LocalDateTime.now();
        if (existingCombo.getStartDate() != null && existingCombo.getEndDate() != null) {
            existingCombo.setActive(!now.isBefore(existingCombo.getStartDate()) && !now.isAfter(existingCombo.getEndDate()));
        } else {
            existingCombo.setActive(false); // Nếu không có ngày, combo không active
        }

        // 5. Lưu và trả về
        return comboRepository.save(existingCombo);
    }


    @Override
    public List<Combo> findActiveCombosContainingBook(String bookId) {
        LocalDateTime now = LocalDateTime.now();

        // Tìm các combo đang active, trong khoảng thời gian, VÀ chứa bookId
        List<Combo> datedCombos = comboRepository
                .findByIsActiveTrueAndStartDateBeforeAndEndDateAfterAndBookIdsContaining(
                        now, now, bookId
                );
        return datedCombos;
    }

    @Override
    public List<Combo> findActiveCombos() {
        // Tác vụ @Scheduled sẽ lo việc cập nhật isActive
        // nên ở đây ta chỉ cần lấy ra là xong.
        return comboRepository.findByIsActiveTrue();
    }
}