package com.tlcn.books.dto;

import lombok.Data;
import java.util.List;

@Data
public class ImportRequestDTO {
    private String id;
    private String bookName;
    private String bookAuthor;
    private String bookSupplier;
    private Integer bookStockQuantity;
    private Double importPrice;

    // Các trường phụ
    private String bookCategory;
    private String mainCategory;

    // --- CÁC TRƯỜNG CHO TÍNH NĂNG GỢI Ý ---
    private String warning;
    private String suggestedBookId;   // Vẫn giữ làm default (cuốn giống nhất)
    private String suggestedBookName; // Vẫn giữ làm default

    // DANH SÁCH CÁC SÁCH GỢI Ý (MỚI)
    private List<SuggestionDTO> suggestions;

    @Data
    public static class SuggestionDTO {
        private String id;
        private String name;
        private int similarity; // % giống

        public SuggestionDTO(String id, String name, int similarity) {
            this.id = id;
            this.name = name;
            this.similarity = similarity;
        }
    }
}