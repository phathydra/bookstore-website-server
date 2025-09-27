package com.tlcn.books.dto;

import lombok.Data;

@Data // Hoặc dùng getters/setters/constructors thông thường
public class BookDetailForOrderDto {
    private String id; // dùng id thay vì bookId để khớp với cách Order Service gọi
    private String categoryName;

    // Cần constructor mặc định cho RestTemplate
    public BookDetailForOrderDto() {}

    public BookDetailForOrderDto(String id, String categoryName) {
        this.id = id;
        this.categoryName = categoryName;
    }

    // Getters và Setters (nếu không dùng Lombok @Data)
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
}