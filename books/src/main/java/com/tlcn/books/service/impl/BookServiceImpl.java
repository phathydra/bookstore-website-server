package com.tlcn.books.service.impl;

import com.tlcn.books.dto.BookDiscountDto;
import com.tlcn.books.dto.BookDto;
import com.tlcn.books.dto.BookWithDiscountDto;
import com.tlcn.books.dto.SearchCriteria;
import com.tlcn.books.entity.Book;
import com.tlcn.books.entity.BookDiscount;
import com.tlcn.books.entity.Discount;
import com.tlcn.books.exception.BookAlreadyExistsException;
import com.tlcn.books.exception.ResourceNotFoundException;
import com.tlcn.books.mapper.BookMapper;
import com.tlcn.books.repository.BookDiscountRepository;
import com.tlcn.books.repository.BookRepository;
import com.tlcn.books.repository.DiscountRepository;
import com.tlcn.books.service.IBookService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class BookServiceImpl implements IBookService {

    private final BookRepository bookRepository;
    private final BookDiscountRepository bookDiscountRepository;
    private final DiscountRepository discountRepository;

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
    public Page<BookWithDiscountDto> getAllBooks(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Book> books = bookRepository.findAllBy(pageable);
        Page<BookWithDiscountDto> bookWithDiscountDto =
                books.map(book -> {
                    BookWithDiscountDto bookWithDiscount =
                            BookMapper.mapToBookWithDiscountDto(book, new BookWithDiscountDto());
                    Optional<BookDiscount> bookDiscount =
                            bookDiscountRepository.findByBookId(bookWithDiscount.getBookId());
                    if(bookDiscount.isPresent()){
                        Optional<Discount> discount = discountRepository.findById(bookDiscount.get().getDiscountId());
                        bookWithDiscount.setPercentage(discount.get().getPercentage());
                        double discountedPrice = Math.ceil(bookWithDiscount.getBookPrice() * (1 - bookWithDiscount.getPercentage() / (double)100));
                        bookWithDiscount.setDiscountedPrice(discountedPrice);
                    }
                    return bookWithDiscount;
                });
        return bookWithDiscountDto;
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
    public Page<BookWithDiscountDto> searchBooks(int page, int size, String input){
        Pageable pageable = PageRequest.of(page, size);
        Page<Book> books = bookRepository.findByBookNameContainingIgnoreCaseOrBookAuthorContainingIgnoreCase(pageable, input, input);
        Page<BookWithDiscountDto> bookWithDiscountDto =
                books.map(book -> {
                    BookWithDiscountDto bookWithDiscount =
                            BookMapper.mapToBookWithDiscountDto(book, new BookWithDiscountDto());
                    Optional<BookDiscount> bookDiscount =
                            bookDiscountRepository.findByBookId(bookWithDiscount.getBookId());
                    if(bookDiscount.isPresent()){
                        Optional<Discount> discount = discountRepository.findById(bookDiscount.get().getDiscountId());
                        bookWithDiscount.setPercentage(discount.get().getPercentage());
                        double discountedPrice = Math.ceil(bookWithDiscount.getBookPrice() * (1 - bookWithDiscount.getPercentage() / (double)100));
                        bookWithDiscount.setDiscountedPrice(discountedPrice);
                    }
                    return bookWithDiscount;
                });
        return bookWithDiscountDto;
    }

    @Override
    public List<BookDto> getRecommendedBooks(String bookId) {
        Optional<Book> optionalBook = bookRepository.findById(bookId);
        if (optionalBook.isEmpty()) {
            throw new ResourceNotFoundException("Không tìm thấy sách với ID: " + bookId);
        }

        Book book = optionalBook.get();

        List<Book> booksByCategory = bookRepository.findTop5ByBookCategoryAndBookIdNot(book.getBookCategory(), bookId);

        List<Book> booksByAuthor = bookRepository.findTop5ByBookAuthorAndBookIdNot(book.getBookAuthor(), bookId);

        List<Book> recommendedBooks = new ArrayList<>();

        // Thêm sách theo thể loại và tác giả
        for (Book b : booksByCategory) {
            if (!recommendedBooks.contains(b)) {
                recommendedBooks.add(b);
            }
        }
        for (Book b : booksByAuthor) {
            if (!recommendedBooks.contains(b)) {
                recommendedBooks.add(b);
            }
        }
        if (recommendedBooks.size() < 3) {
            List<Book> additionalBooks = (List<Book>) bookRepository.findTop3ByOrderByBookNameAsc();
            for (Book additionalBook : additionalBooks) {
                if (!recommendedBooks.contains(additionalBook) && !additionalBook.getBookId().equals(bookId)) {
                    recommendedBooks.add(additionalBook);
                }
                if (recommendedBooks.size() >= 3) break;
            }
        }
        return recommendedBooks.stream()
                .limit(5)
                .map(b -> BookMapper.mapToBookDto(b, new BookDto()))
                .collect(Collectors.toList());
    }
    @Override
    public Page<BookWithDiscountDto> getBooksByMainCategory(String mainCategory, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Book> books = bookRepository.findByMainCategory(mainCategory, pageable);
        Page<BookWithDiscountDto> bookWithDiscountDto =
                books.map(book -> {
                    BookWithDiscountDto bookWithDiscount =
                            BookMapper.mapToBookWithDiscountDto(book, new BookWithDiscountDto());
                    Optional<BookDiscount> bookDiscount =
                            bookDiscountRepository.findByBookId(bookWithDiscount.getBookId());
                    if(bookDiscount.isPresent()){
                        Optional<Discount> discount = discountRepository.findById(bookDiscount.get().getDiscountId());
                        bookWithDiscount.setPercentage(discount.get().getPercentage());
                        double discountedPrice = Math.ceil(bookWithDiscount.getBookPrice() * (1 - bookWithDiscount.getPercentage() / (double)100));
                        bookWithDiscount.setDiscountedPrice(discountedPrice);
                    }
                    return bookWithDiscount;
                });
        return bookWithDiscountDto;
    }
    @Override
    public Page<BookWithDiscountDto> getBooksByBookCategory(String bookCategory, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Book> books = bookRepository.findByBookCategory(bookCategory, pageable);
        Page<BookWithDiscountDto> bookWithDiscountDto =
                books.map(book -> {
                    BookWithDiscountDto bookWithDiscount =
                            BookMapper.mapToBookWithDiscountDto(book, new BookWithDiscountDto());
                    Optional<BookDiscount> bookDiscount =
                            bookDiscountRepository.findByBookId(bookWithDiscount.getBookId());
                    if(bookDiscount.isPresent()){
                        Optional<Discount> discount = discountRepository.findById(bookDiscount.get().getDiscountId());
                        bookWithDiscount.setPercentage(discount.get().getPercentage());
                        double discountedPrice = Math.ceil(bookWithDiscount.getBookPrice() * (1 - bookWithDiscount.getPercentage() / (double)100));
                        bookWithDiscount.setDiscountedPrice(discountedPrice);
                    }
                    return bookWithDiscount;
                });
        return bookWithDiscountDto;
    }

}
