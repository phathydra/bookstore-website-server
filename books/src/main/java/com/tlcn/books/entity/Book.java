package com.tlcn.books.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.*;
import java.util.List;
import org.springframework.data.mongodb.core.index.Indexed;

@Document(collection = "books")
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class Book {

    @Id
    private String bookId;

    private String bookName;

    private String bookAuthor;

    private List<String> bookImages;  // đổi từ String thành List<String>

    private Double bookPrice;

    private String mainCategory;

    private String bookCategory;

    private int bookYearOfProduction;

    private String bookPublisher;

    private String bookLanguage;

    private int bookStockQuantity;

    private String bookSupplier;

    private String bookDescription;

    public void setImportPrice(Double importPrice) {
    }

    @Indexed
    private List<String> tags; // Ví dụ: ["HOT_SELLER", "COLD_SELLER", "LOW_STOCK"]
}
