package com.bookstore.orders.dto;

import lombok.Data;
import java.util.List;

@Data
public class BookDataForCartDto {
    private String bookId;
    private String bookName;
    private List<String> bookImages;
    private Double price;
    private String bookAuthor;

    private String mainCategory;
    private List<String> tags;
}