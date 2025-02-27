package com.tlcn.books.service;

import com.tlcn.books.dto.BookDto;
import com.tlcn.books.dto.SearchCriteria;

import java.util.List;

public interface IBookService {

    /**
     *
     * @param bookDto - BookDto Object
     */
    void createBook(BookDto bookDto);

    void updateBook(String bookId, BookDto bookDto);

    void deleteBook(String bookId);

    List<BookDto> getAllBooks();

    BookDto getBookById(String bookId);

    List<BookDto> searchBooks(SearchCriteria searchCriteria);
}
