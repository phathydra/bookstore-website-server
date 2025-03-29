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

    BookWithDiscountDto getBookById(String bookId);

    Page<BookWithDiscountDto> searchBooks(int page, int size, String input);

    List<BookDto> getRecommendedBooks(String bookId);

    List<BookDto> getSearchRecommendedBooks(String bookName, List<String> excludedBooks);

    List<BookDto> getDiscountedBooks(String discountId);

    Page<BookWithDiscountDto> getBooksByMainCategory(String mainCategory, int page, int size);
    Page<BookWithDiscountDto> getBooksByBookCategory(String bookCategory, int page, int size);

    Page<BookWithDiscountDto> filterBooks(String bookAuthor, List<String> mainCategory, Double minPrice, Double maxPrice, List<String> bookPublisher, List<String> bookSupplier, int page, int size);
}
