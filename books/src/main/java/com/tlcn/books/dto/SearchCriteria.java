package com.tlcn.books.dto;

import lombok.Data;

@Data
public class SearchCriteria {
    private String bookName;
    private String bookAuthor;
    private String bookCategory;
    private String bookPublisher;
    private String bookLanguage;
// Thêm các tiêu chí tìm kiếm khác nếu cần
}
