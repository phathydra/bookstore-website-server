package com.tlcn.books.service;

import com.tlcn.books.dto.*;
import com.tlcn.books.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

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

    List<BookDto> getDiscountedBooksAdmin(String discountId);

    Page<BookWithDiscountDto> getAllDiscountedBooks(int page, int size);

    ByteArrayInputStream exportDiscountedBooks(String discountId) throws IOException;

    Page<BookWithDiscountDto> getBooksByMainCategory(String mainCategory, int page, int size);
    Page<BookWithDiscountDto> getBooksByBookCategory(String bookCategory, int page, int size);

    Page<BookWithDiscountDto> filterBooks(BookFilterInputDto input, int page, int size);

    void decreaseStock(String bookId, int quantity);

    void increaseStock(String bookId, int quantity);

    ByteArrayInputStream exportAllBooks() throws IOException;

    void importBooks(MultipartFile file) throws IOException;

    Page<BookWithDiscountDto> getBooksByStockQuantity(int quantity, int page, int size);

    Page<BookWithDiscountDto> getBooksInStock(int page, int size);

    ByteArrayInputStream exportBooksInStock() throws IOException;
    ByteArrayInputStream exportBooksOutOfStock() throws IOException;
    void importStock(List<BookDto> booksToImport);
    List<BookDataForCartDto> getBookDetailsByIds(List<String> bookIds);
    List<BookDetailDto> getAllBookDetails();
    void updateTags(Map<String, List<String>> tagUpdates);
    List<Book> findAllByIds(List<String> bookIds);
    Page<BookWithDiscountDto> getBooksByAuthor(String author, int page, int size);
}
