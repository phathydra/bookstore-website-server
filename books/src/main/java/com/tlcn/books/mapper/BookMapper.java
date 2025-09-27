package com.tlcn.books.mapper;

import com.tlcn.books.dto.BookDto;
import com.tlcn.books.dto.BookDetailDto; // <--- CẦN ĐẢM BẢO ĐÃ IMPORT DTO NÀY
import com.tlcn.books.dto.BookWithDiscountDto;
import com.tlcn.books.entity.Book;

public class BookMapper {
    public static BookDto mapToBookDto(Book book, BookDto bookDto) {

        bookDto.setBookId(book.getBookId());
        bookDto.setBookName(book.getBookName());
        bookDto.setBookAuthor(book.getBookAuthor());
        bookDto.setBookImages(book.getBookImages()); // đổi sang List<String>
        bookDto.setBookPrice(book.getBookPrice());
        bookDto.setMainCategory(book.getMainCategory());
        bookDto.setBookCategory(book.getBookCategory());
        bookDto.setBookYearOfProduction(book.getBookYearOfProduction());
        bookDto.setBookPublisher(book.getBookPublisher());
        bookDto.setBookLanguage(book.getBookLanguage());
        bookDto.setBookStockQuantity(book.getBookStockQuantity());
        bookDto.setBookSupplier(book.getBookSupplier());
        bookDto.setBookDescription(book.getBookDescription());

        return bookDto;
    }

    public static Book mapToBook(BookDto bookDto, Book book) {
        book.setBookId(bookDto.getBookId());
        book.setBookName(bookDto.getBookName());
        book.setBookAuthor(bookDto.getBookAuthor());
        book.setBookImages(bookDto.getBookImages()); // đổi sang List<String>
        book.setBookPrice(bookDto.getBookPrice());
        book.setMainCategory(bookDto.getMainCategory());
        book.setBookCategory(bookDto.getBookCategory());
        book.setBookYearOfProduction(bookDto.getBookYearOfProduction());
        book.setBookPublisher(bookDto.getBookPublisher());
        book.setBookLanguage(bookDto.getBookLanguage());
        book.setBookStockQuantity(bookDto.getBookStockQuantity());
        book.setBookSupplier(bookDto.getBookSupplier());
        book.setBookDescription(bookDto.getBookDescription());
        return book;
    }

    public static BookWithDiscountDto mapToBookWithDiscountDto(Book book, BookWithDiscountDto bookWithDiscountDto){
        bookWithDiscountDto.setBookId(book.getBookId());
        bookWithDiscountDto.setBookName(book.getBookName());
        bookWithDiscountDto.setBookAuthor(book.getBookAuthor());
        bookWithDiscountDto.setBookImages(book.getBookImages()); // đổi sang List<String>
        bookWithDiscountDto.setBookPrice(book.getBookPrice());
        bookWithDiscountDto.setMainCategory(book.getMainCategory());
        bookWithDiscountDto.setBookCategory(book.getBookCategory());
        bookWithDiscountDto.setBookYearOfProduction(book.getBookYearOfProduction());
        bookWithDiscountDto.setBookPublisher(book.getBookPublisher());
        bookWithDiscountDto.setBookLanguage(book.getBookLanguage());
        bookWithDiscountDto.setBookStockQuantity(book.getBookStockQuantity());
        bookWithDiscountDto.setBookSupplier(book.getBookSupplier());
        bookWithDiscountDto.setBookDescription(book.getBookDescription());

        return bookWithDiscountDto;
    }

    /**
     * PHƯƠNG THỨC MỚI: Dành cho Order Service lấy chi tiết tồn kho và thể loại.
     * Sửa lỗi bookStockQuantity = 0 và bookCategory = null.
     */
    public static BookDetailDto mapToBookDetailDto(Book book, BookDetailDto bookDetailDto){
        bookDetailDto.setId(book.getBookId()); // ID là bookId trong BookDetailDto
        bookDetailDto.setBookName(book.getBookName());

        // Lấy tên thể loại (thường là bookCategory hoặc mainCategory, tôi dùng bookCategory)
        bookDetailDto.setBookCategory(book.getBookCategory());

        // LẤY SỐ LƯỢNG TỒN KHO THỰC TẾ
        bookDetailDto.setBookStockQuantity(book.getBookStockQuantity());

        return bookDetailDto;
    }
}