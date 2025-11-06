package com.bookstore.orders.service;

import com.bookstore.orders.dto.ComboDto;
import com.bookstore.orders.entity.Combo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IComboService {

    /**
     * Tạo một combo mới
     * @param comboDto Dữ liệu combo từ admin
     */
    void createCombo(ComboDto comboDto);

    /**
     * Lấy danh sách tất cả combo (phân trang)
     * @param pageable Thông tin phân trang
     * @return Trang (Page) chứa các Combo
     */
    Page<Combo> getAllCombos(Pageable pageable);

    /**
     * Xóa một combo theo ID
     * @param comboId ID của combo cần xóa
     */
    void deleteCombo(String comboId);

    /**
     * Tìm các combo đang hoạt động và chứa một bookId cụ thể
     * @param bookId ID của sách cần tìm
     * @return Danh sách các Combo phù hợp
     */
    List<Combo> findActiveCombosContainingBook(String bookId);

    /**
     * Lấy chi tiết combo bằng ID.
     * @param comboId ID của combo
     * @return Đối tượng Combo
     * @throws RuntimeException nếu không tìm thấy
     */
    Combo getComboById(String comboId);

    /**
     * Cập nhật một combo hiện có.
     * @param comboId ID của combo cần cập nhật
     * @param comboDto Dữ liệu mới
     * @return Đối tượng Combo đã được cập nhật
     * @throws RuntimeException nếu không tìm thấy
     */
    Combo updateCombo(String comboId, ComboDto comboDto);
    List<Combo> findActiveCombos();
}