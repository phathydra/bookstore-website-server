package com.tlcn.books.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.*;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Document(collection = "books")
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class Book{

    @Id
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
