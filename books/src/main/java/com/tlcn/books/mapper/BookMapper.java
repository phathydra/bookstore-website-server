package com.tlcn.books.mapper;

import com.tlcn.books.dto.BookDto;
import com.tlcn.books.dto.BookWithDiscountDto;
import com.tlcn.books.entity.Book;

public class BookMapper {
    public static BookDto mapToBookDto(Book book, BookDto bookDto) {

        // Gán giá trị từ Book sang BookDto
        bookDto.setBookId(book.getBookId());
        bookDto.setBookName(book.getBookName());
        bookDto.setBookAuthor(book.getBookAuthor());
        bookDto.setBookImage(book.getBookImage());
        bookDto.setBookPrice(book.getBookPrice());
        bookDto.setMainCategory(book.getMainCategory());
        bookDto.setBookCategory(book.getBookCategory());
        bookDto.setBookYearOfProduction(book.getBookYearOfProduction());
        bookDto.setBookPublisher(book.getBookPublisher());
        bookDto.setBookLanguage(book.getBookLanguage());
        bookDto.setBookStockQuantity(book.getBookStockQuantity());
        bookDto.setBookSupplier(book.getBookSupplier());
        bookDto.setBookDescription(book.getBookDescription());

        // Trả về đối tượng BookDto đã được map
        return bookDto;
    }

    public static Book mapToBook(BookDto bookDto, Book book) {
        book.setBookId(bookDto.getBookId());
        book.setBookName(bookDto.getBookName());
        book.setBookAuthor(bookDto.getBookAuthor());
        book.setBookImage(bookDto.getBookImage());
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
        bookWithDiscountDto.setBookImage(book.getBookImage());
        bookWithDiscountDto.setBookPrice(book.getBookPrice());
        bookWithDiscountDto.setMainCategory(book.getMainCategory());
        bookWithDiscountDto.setBookCategory(book.getBookCategory());
        bookWithDiscountDto.setBookYearOfProduction(book.getBookYearOfProduction());
        bookWithDiscountDto.setBookPublisher(book.getBookPublisher());
        bookWithDiscountDto.setBookLanguage(book.getBookLanguage());
        bookWithDiscountDto.setBookStockQuantity(book.getBookStockQuantity());
        bookWithDiscountDto.setBookSupplier(book.getBookSupplier());
        bookWithDiscountDto.setBookDescription(book.getBookDescription());

        // Trả về đối tượng BookDto đã được map
        return bookWithDiscountDto;
    }
}
