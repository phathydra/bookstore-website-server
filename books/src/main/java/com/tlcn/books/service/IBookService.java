package com.tlcn.books.service;

import com.tlcn.books.dto.BookDto;
import com.tlcn.books.dto.BookWithDiscountDto;
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

    Page<BookWithDiscountDto> getAllBooks(int page, int size);

    BookDto getBookById(String bookId);

    Page<BookWithDiscountDto> searchBooks(int page, int size, String input);

    List<BookDto> getRecommendedBooks(String bookId);

    Page<BookWithDiscountDto> getBooksByMainCategory(String mainCategory, int page, int size);

<<<<<<< HEAD
    Page<BookWithDiscountDto> getBooksByBookCategory(String bookCategory, int page, int size);
=======
    Page<BookDto> getBooksByBookCategory(String bookCategory, int page, int size);

    Page<BookDto> filterBooks(String bookAuthor, List<String> mainCategory, Double minPrice, Double maxPrice, List<String> bookPublisher, List<String> bookSupplier, int page, int size);

>>>>>>> 40dbc8dab2adb07d67065ee7964261668d0cb069
}
