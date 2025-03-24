package com.tlcn.books.controller;

import com.tlcn.books.Constants.BookConstants;
import com.tlcn.books.dto.BookDto;
import com.tlcn.books.dto.BookWithDiscountDto;
import com.tlcn.books.dto.ResponseDto;
import com.tlcn.books.dto.SearchCriteria;
import com.tlcn.books.service.IBookService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Collections;

@CrossOrigin(origins = "http://localhost:3001")
@RestController
@RequestMapping(path = "/api/book", produces = {MediaType.APPLICATION_JSON_VALUE})
public class BookController {
    private IBookService iBookService;

    @Autowired  // Cho phép Spring inject dependency
    public BookController(IBookService iBookService) {
        this.iBookService = iBookService;
    }

    @PostMapping("")
    public ResponseEntity<ResponseDto> createBook(@Valid @RequestBody BookDto bookDto) {
        try {
            iBookService.createBook(bookDto);
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(new ResponseDto(BookConstants.STATUS_201, BookConstants.MESSAGE_201));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDto(BookConstants.STATUS_500, "Lỗi máy chủ nội bộ: " + e.getMessage()));
        }
    }

    // Thêm API cập nhật sách
    @PutMapping("/{bookId}")
    public ResponseEntity<ResponseDto> updateBook(@PathVariable String bookId, @Valid @RequestBody BookDto bookDto) {
        try {
            iBookService.updateBook(bookId, bookDto);
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(new ResponseDto(BookConstants.STATUS_200, "Cập nhật sách thành công"));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDto(BookConstants.STATUS_500, "Lỗi cập nhật sách: " + e.getMessage()));
        }
    }

    // API Xóa sách
    @DeleteMapping("/{bookId}")
    public ResponseEntity<ResponseDto> deleteBook(@PathVariable String bookId) {
        try {
            iBookService.deleteBook(bookId);
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(new ResponseDto(BookConstants.STATUS_200, "Xóa sách thành công"));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDto(BookConstants.STATUS_500, "Lỗi xóa sách: " + e.getMessage()));
        }
    }

    @GetMapping("")
    public ResponseEntity<Page<BookWithDiscountDto>> getAllBooks(@RequestParam(defaultValue = "0") int page,
                                                                 @RequestParam(defaultValue = "10") int size) {
        try {
            Page<BookWithDiscountDto> books = iBookService.getAllBooks(page, size);
            return ResponseEntity.ok(books);
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Page.empty());
        }
    }

    @GetMapping("/{bookId}")
    public ResponseEntity<BookDto> getBookById(@PathVariable String bookId) {
        try {
            BookDto bookDto = iBookService.getBookById(bookId);
            return ResponseEntity.ok(bookDto);
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .build();
        }
    }

    @PostMapping("/search")
    public ResponseEntity<Page<BookWithDiscountDto>> searchBooks(@RequestParam(defaultValue = "0") int page,
                                                     @RequestParam(defaultValue = "10") int size,
                                                     @RequestParam String input) {
        try {
            Page<BookWithDiscountDto> books = iBookService.searchBooks(page, size, input);
            return ResponseEntity.ok(books);
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Page.empty());
        }
    }
    @GetMapping("/{bookId}/recommendations")
    public ResponseEntity<List<BookDto>> getRecommendedBooks(@PathVariable String bookId) {
        try {
            List<BookDto> recommendedBooks = iBookService.getRecommendedBooks(bookId);
            return ResponseEntity.ok(recommendedBooks);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
        }
    }

    @GetMapping("/mainCategory/{mainCategory}")
    public ResponseEntity<Page<BookWithDiscountDto>> getBooksByMainCategory(@PathVariable String mainCategory,
                                                                @RequestParam(defaultValue = "0") int page,
                                                                @RequestParam(defaultValue = "10") int size) {
        try {
            Page<BookWithDiscountDto> books = iBookService.getBooksByMainCategory(mainCategory, page, size);
            return ResponseEntity.ok(books);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Page.empty());
        }
    }

    @GetMapping("/bookCategory/{bookCategory}")
    public ResponseEntity<Page<BookWithDiscountDto>> getBooksByBookCategory(@PathVariable String bookCategory,
                                                                @RequestParam(defaultValue = "0") int page,
                                                                @RequestParam(defaultValue = "10") int size) {
        try {
            Page<BookWithDiscountDto> books = iBookService.getBooksByBookCategory(bookCategory, page, size);
            return ResponseEntity.ok(books);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Page.empty());
        }
    }

    @GetMapping("/filter")
    public ResponseEntity<Page<BookWithDiscountDto>> filterBooks(
            @RequestParam(required = false, defaultValue = "") String bookAuthor,
            @RequestParam(required = false) List<String> mainCategory,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) List<String> bookPublisher, // Thay đổi thành List<String>
            @RequestParam(required = false) List<String> bookSupplier,  // Thay đổi thành List<String>
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<BookWithDiscountDto> books = iBookService.filterBooks(bookAuthor, mainCategory, minPrice, maxPrice, bookPublisher, bookSupplier, page, size);
        return new ResponseEntity<>(books, HttpStatus.OK);
    }


}
