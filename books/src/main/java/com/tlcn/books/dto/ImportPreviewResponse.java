package com.tlcn.books.dto;

import lombok.Data;
import java.util.List;

@Data
public class ImportPreviewResponse {
    // Danh sách các sách hệ thống phát hiện là MỚI
    private List<ImportRequestDTO> newBooks;

    // Danh sách các sách hệ thống phát hiện là ĐÃ CÓ (chỉ cần cộng dồn số lượng)
    private List<ImportRequestDTO> existingBooks;
}