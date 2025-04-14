package com.bookstore.chatbot.entity;

import lombok.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class Book {

    private String bookId;

    private String bookName;

    private String bookAuthor;

    private String bookImage;

    private Double bookPrice;

    private String mainCategory;

    private String bookCategory;

    private int bookYearOfProduction;

    private String bookPublisher;

    private String bookLanguage;

    private int bookStockQuantity;

    private String bookSupplier;

    private String bookDescription;
}
