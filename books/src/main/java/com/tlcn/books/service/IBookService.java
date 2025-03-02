package com.tlcn.books.service;

import com.tlcn.books.dto.BookDto;
import com.tlcn.books.dto.SearchCriteria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IBookService {

    /**
     *
     * @param bookDto - BookDto Object
     */
    void createBook(BookDto bookDto);

    void updateBook(String bookId, BookDto bookDto);

    void deleteBook(String bookId);

    Page<BookDto> getAllBooks(int page, int size);

    BookDto getBookById(String bookId);

    Page<BookDto> searchBooks(int page, int size, String input);
}
