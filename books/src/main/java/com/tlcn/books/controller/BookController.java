package com.tlcn.books.controller;

import com.tlcn.books.Constants.BookConstants;
import com.tlcn.books.dto.BookDto;
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
    public ResponseEntity<Page<BookDto>> getAllBooks(@RequestParam(defaultValue = "0") int page,
                                                     @RequestParam(defaultValue = "10") int size) {
        try {
            Page<BookDto> books = iBookService.getAllBooks(page, size);
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
    public ResponseEntity<List<BookDto>> searchBooks(@RequestParam(defaultValue = "0") int page,
                                                     @RequestParam(defaultValue = "10") int size,
                                                     @RequestBody SearchCriteria searchCriteria) {
        try {
            List<BookDto> books = iBookService.searchBooks(page, size, searchCriteria);
            return ResponseEntity.ok(books);
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.emptyList());
        }
    }
}
