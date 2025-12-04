package com.tlcn.books.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookDataForCartDto {
    private String bookId;
    private String bookName;
    private List<String> bookImages;
    private Double price; // Đây là giá bán cuối cùng (đã áp dụng discount nếu có)

    private String bookAuthor;
    private String mainCategory;
    private List<String> tags;
}