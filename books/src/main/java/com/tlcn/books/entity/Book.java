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

    private BigDecimal bookPrice;

    private String mainCategory;

    private String bookCategory;

    private int bookYearOfProduction;

    private String bookPublisher;

    private String bookLanguage;

    private int bookStockQuantity;

    private String bookSupplier;

    private String bookDescription;

    // Constructor nhận double để tránh lỗi khi nhận dữ liệu từ JSON hoặc DB
    public Book(String bookId, String bookName, String bookAuthor, String bookImage,
                double bookPrice, String mainCategory, String bookCategory, int bookYearOfProduction,
                String bookPublisher, String bookLanguage, int bookStockQuantity,
                String bookSupplier, String bookDescription) {
        this.bookId = bookId;
        this.bookName = bookName;
        this.bookAuthor = bookAuthor;
        this.bookImage = bookImage;
        this.bookPrice = BigDecimal.valueOf(bookPrice).setScale(2, RoundingMode.HALF_UP);
        this.mainCategory = mainCategory;
        this.bookCategory = bookCategory;
        this.bookYearOfProduction = bookYearOfProduction;
        this.bookPublisher = bookPublisher;
        this.bookLanguage = bookLanguage;
        this.bookStockQuantity = bookStockQuantity;
        this.bookSupplier = bookSupplier;
        this.bookDescription = bookDescription;
    }

    // Đảm bảo BigDecimal luôn có 2 số thập phân khi set giá trị
    public void setBookPrice(BigDecimal bookPrice) {
        this.bookPrice = bookPrice.setScale(2, RoundingMode.HALF_UP);
    }

    // Overloaded setter để nhận double
    public void setBookPrice(double bookPrice) {
        this.bookPrice = BigDecimal.valueOf(bookPrice).setScale(2, RoundingMode.HALF_UP);
    }
}
