package com.tlcn.books.service;

import com.tlcn.books.dto.BookSummaryResponseDto;

public interface IBookSummaryService {
    BookSummaryResponseDto getBookSummary(String title, String author);
}
