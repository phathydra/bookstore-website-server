package com.tlcn.books.dto;

public class BookSummaryResponseDto {
    private String title;
    private String author;
    private String summary;

    public BookSummaryResponseDto() {}

    public BookSummaryResponseDto(String title, String author, String summary) {
        this.title = title;
        this.author = author;
        this.summary = summary;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }
}
