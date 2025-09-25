package com.bookstore.chatbot.dto;

import lombok.Data;

import java.util.List;

@Data
public class BookFilterInputDto {
    private String bookAuthor;
    private List<String> mainCategory;
    private List<String> bookCategory;
    private Double minPrice;
    private Double maxPrice;
    private List<String> bookPublisher;
    private List<String> bookSupplier;
}
