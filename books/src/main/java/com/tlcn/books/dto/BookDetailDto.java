package com.tlcn.books.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BookDetailDto {
    private String id;
    private String bookName;
    private String bookCategory;
    private int bookStockQuantity;

    public BookDetailDto(String id, String bookName) {
        this.id = id;
        this.bookName = bookName;
    }
}