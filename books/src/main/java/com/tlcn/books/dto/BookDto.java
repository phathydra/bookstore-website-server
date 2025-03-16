package com.tlcn.books.dto;

import jakarta.validation.constraints.*;

import lombok.Data;

@Data
public class BookDto {

    private String bookId;

    @NotEmpty(message = "Tên sách không được để trống")
    @Size(min = 2, max = 100, message = "Tên sách phải có từ 2 đến 100 ký tự")
    private String bookName;

    @NotEmpty(message = "Tác giả không được để trống")
    private String bookAuthor;

    private String bookImage;

    @Min(value = 0, message = "Giá sách phải lớn hơn hoặc bằng 0")
    private double bookPrice;

    @NotEmpty(message = "Danh mục chính không được để trống")
    private String mainCategory; // Thêm danh mục chính

    @NotEmpty(message = "Thể loại không được để trống")
    private String bookCategory;

    @Min(value = 1900, message = "Năm sản xuất phải lớn hơn hoặc bằng 1900")
    @Max(value = 2025, message = "Năm sản xuất không được lớn hơn 2025")
    private int bookYearOfProduction;

    @NotEmpty(message = "Nhà xuất bản không được để trống")
    private String bookPublisher;

    @NotEmpty(message = "Ngôn ngữ không được để trống")
    private String bookLanguage;

    @Min(value = 0, message = "Số lượng tồn kho không được nhỏ hơn 0")
    private int bookStockQuantity;

    @NotEmpty(message = "Nhà cung cấp không được để trống")
    @Size(max = 100, message = "Nhà cung cấp không được vượt quá 100 ký tự")
    private String bookSupplier;

    @Size(max = 500, message = "Mô tả sách không được vượt quá 500 ký tự")
    private String bookDescription;
}

