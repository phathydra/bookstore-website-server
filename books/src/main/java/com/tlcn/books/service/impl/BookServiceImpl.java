package com.tlcn.books.service.impl;

import com.tlcn.books.dto.BookDto;
import com.tlcn.books.dto.SearchCriteria;
import com.tlcn.books.entity.Book;
import com.tlcn.books.exception.BookAlreadyExistsException;
import com.tlcn.books.exception.ResourceNotFoundException;
import com.tlcn.books.mapper.BookMapper;
import com.tlcn.books.repository.BookRepository;
import com.tlcn.books.service.IBookService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class BookServiceImpl implements IBookService {

    private final BookRepository bookRepository;

    @Override
    public void createBook(BookDto bookDto) {
        Book book = BookMapper.mapToBook(bookDto, new Book());
        if (bookDto.getBookId() != null && bookRepository.existsById(bookDto.getBookId())) {
            throw new BookAlreadyExistsException("Trùng ID: " + bookDto.getBookId());
        }
        Book savedBook = bookRepository.save(book);
        System.out.println("Saved Book ID: " + savedBook.getBookId());
    }
    @Override
    public void updateBook(String bookId, BookDto bookDto) {
        Optional<Book> optionalBook = bookRepository.findById(bookId);
        if (optionalBook.isEmpty()) {
            throw new ResourceNotFoundException("Không tìm thấy sách với ID: " + bookId);
        }

        Book existingBook = optionalBook.get();
        existingBook.setBookName(bookDto.getBookName());
        existingBook.setBookAuthor(bookDto.getBookAuthor());
        existingBook.setBookImage(bookDto.getBookImage());
        existingBook.setBookPrice(bookDto.getBookPrice());
        existingBook.setBookCategory(bookDto.getBookCategory());
        existingBook.setBookYearOfProduction(bookDto.getBookYearOfProduction());
        existingBook.setBookPublisher(bookDto.getBookPublisher());
        existingBook.setBookLanguage(bookDto.getBookLanguage());
        existingBook.setBookStockQuantity(bookDto.getBookStockQuantity());
        existingBook.setBookSupplier(bookDto.getBookSupplier());
        existingBook.setBookDescription(bookDto.getBookDescription());

        bookRepository.save(existingBook);
    }

    @Override
    public void deleteBook(String bookId) {
        Optional<Book> optionalBook = bookRepository.findById(bookId);
        if (optionalBook.isEmpty()) {
            throw new ResourceNotFoundException("Không tìm thấy sách với ID: " + bookId);
        }

        bookRepository.deleteById(bookId);
        System.out.println("Đã xóa sách với ID: " + bookId);
    }

    @Override
    public List<BookDto> getAllBooks() {
        List<Book> books = bookRepository.findAll();
        return books.stream()
                .map(book -> BookMapper.mapToBookDto(book, new BookDto()))
                .collect(Collectors.toList());
    }

    @Override
    public BookDto getBookById(String bookId) {
        Optional<Book> optionalBook = bookRepository.findById(bookId);
        if (optionalBook.isEmpty()) {
            throw new RuntimeException("Không tìm thấy sách với ID: " + bookId);
        }
        return BookMapper.mapToBookDto(optionalBook.get(), new BookDto());
    }

    @Override
    public List<BookDto> searchBooks(SearchCriteria searchCriteria) {
        return getAllBooks().stream()
                .filter(book -> (searchCriteria.getBookName() == null || book.getBookName().equalsIgnoreCase(searchCriteria.getBookName())) &&
                        (searchCriteria.getBookAuthor() == null || book.getBookAuthor().equalsIgnoreCase(searchCriteria.getBookAuthor())) &&
                        (searchCriteria.getBookCategory() == null || book.getBookCategory().equalsIgnoreCase(searchCriteria.getBookCategory())) &&
                        (searchCriteria.getBookPublisher() == null || book.getBookPublisher().equalsIgnoreCase(searchCriteria.getBookPublisher())) &&
                        (searchCriteria.getBookLanguage() == null || book.getBookLanguage().equalsIgnoreCase(searchCriteria.getBookLanguage())))
                .collect(Collectors.toList());
    }

}
