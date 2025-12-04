package com.tlcn.books.controller;

import com.tlcn.books.constants.BookConstants;
import com.tlcn.books.dto.*;
import com.tlcn.books.entity.Book;
import com.tlcn.books.exception.ResourceNotFoundException;
import com.tlcn.books.service.IBookService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Collections;
import java.util.Map;


@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"}, exposedHeaders = "Content-Disposition")
@RestController
@RequestMapping(path = "/api/book", produces = {MediaType.APPLICATION_JSON_VALUE})
public class BookController {
    private IBookService iBookService;

    @Autowired  // Cho phép Spring inject dependency
    public BookController(IBookService iBookService) {
        this.iBookService = iBookService;
    }

    @PostMapping("")
    public ResponseEntity<ResponseDto> createBook(@RequestBody BookDto bookDto) { // Bỏ @Valid
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
    public ResponseEntity<ResponseDto> updateBook(@PathVariable String bookId, @RequestBody BookDto bookDto) {
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
    public ResponseEntity<BookWithDiscountDto> getBookById(@PathVariable String bookId) {
        try {
            BookWithDiscountDto bookDto = iBookService.getBookById(bookId);
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

    @PostMapping("/search_recommended")
    public ResponseEntity<List<BookDto>> getSearchRecommendedBooks(@RequestParam String bookName, @RequestBody List<String> excludedBooks){
        try{
            List<BookDto> books = iBookService.getSearchRecommendedBooks(bookName, excludedBooks);
            return ResponseEntity.ok(books);
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.emptyList());
        }
    }

    @GetMapping("/discounted_books_admin")
    public ResponseEntity<List<BookDto>> getDiscountedBooksAdmin(@RequestParam String discountId){
        try{
            List<BookDto> books = iBookService.getDiscountedBooksAdmin(discountId);
            return ResponseEntity.ok(books);
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.emptyList());
        }
    }

    @GetMapping("/discounted_books")
    public ResponseEntity<Page<BookWithDiscountDto>> getAllDiscountedBooks(@RequestParam int page, @RequestParam int size){
        try{
            Page<BookWithDiscountDto> books = iBookService.getAllDiscountedBooks(page, size);
            return ResponseEntity.ok(books);
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Page.empty());
        }
    }

    @GetMapping("/export_discounted_books")
    public ResponseEntity<Resource> exportDiscountedBooks(@RequestParam String discountId){
        try{
            ByteArrayInputStream in = iBookService.exportDiscountedBooks(discountId);
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Disposition", "attachment; filename=discounted_books.xlsx");
            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
                    .body(new InputStreamResource(in));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .build();
        }
    }

    @GetMapping("/{bookId}/recommendations")
    public ResponseEntity<List<BookDto>> getRecommendedBooks(@PathVariable String bookId, @RequestParam String accountId, @RequestParam int k) {
        try {
            List<BookDto> recommendedBooks = iBookService.getRecommendedBooks(bookId, accountId, k);
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

    @PostMapping("/filter")
    public ResponseEntity<Page<BookWithDiscountDto>> filterBooks(
            @Valid @RequestBody BookFilterInputDto input,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<BookWithDiscountDto> books = iBookService.filterBooks(input, page, size);
        return new ResponseEntity<>(books, HttpStatus.OK);
    }

    @PutMapping("/{bookId}/decrease-stock")
    public ResponseEntity<ResponseDto> decreaseStock(@PathVariable String bookId, @RequestBody DecreaseStockRequest request) {
        try {
            iBookService.decreaseStock(bookId, request.getQuantity());
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(new ResponseDto(BookConstants.STATUS_200, "Đã giảm số lượng sách"));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(new ResponseDto(BookConstants.STATUS_404, e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new ResponseDto(BookConstants.STATUS_400, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDto(BookConstants.STATUS_500, "Lỗi khi giảm số lượng sách: " + e.getMessage()));
        }
    }
    @PutMapping("/{bookId}/increase-stock")
    public ResponseEntity<ResponseDto> increaseStock(@PathVariable String bookId, @RequestBody IncreaseStockRequest request) {
        try {
            iBookService.increaseStock(bookId, request.getQuantity());
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(new ResponseDto(BookConstants.STATUS_200, "Đã tăng số lượng sách"));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(new ResponseDto(BookConstants.STATUS_404, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDto(BookConstants.STATUS_500, "Lỗi khi tăng số lượng sách: " + e.getMessage()));
        }
    }

    public static class DecreaseStockRequest {
        private int quantity;

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }
    }

    public static class IncreaseStockRequest {
        private int quantity;

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }
    }

    @PostMapping("/import")
    public ResponseEntity<String> importBooks(@RequestParam("file") MultipartFile file) {
        try {
            iBookService.importBooks(file);
            return ResponseEntity.ok("Import sách thành công!");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi import: " + e.getMessage());
        }
    }

    @GetMapping("/export_books")
    public ResponseEntity<Resource> exportBooks(@RequestParam(defaultValue = "all") String filter) {
        try {
            ByteArrayInputStream in;

            switch (filter) {
                case "in-stock":
                    in = iBookService.exportBooksInStock();
                    break;
                case "out-of-stock":
                    in = iBookService.exportBooksOutOfStock();
                    break;
                default:
                    in = iBookService.exportAllBooks();
                    break;
            }

            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Disposition", "attachment; filename=books.xlsx");

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType.parseMediaType(
                            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(new InputStreamResource(in));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/out-of-stock")
    public ResponseEntity<Page<BookWithDiscountDto>> getOutOfStockBooks(@RequestParam(defaultValue = "0") int page,
                                                                        @RequestParam(defaultValue = "10") int size) {
        try {
            Page<BookWithDiscountDto> books = iBookService.getBooksByStockQuantity(0, page, size);
            return ResponseEntity.ok(books);
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Page.empty());
        }
    }

    @GetMapping("/in-stock")
    public ResponseEntity<Page<BookWithDiscountDto>> getInStockBooks(@RequestParam(defaultValue = "0") int page,
                                                                     @RequestParam(defaultValue = "10") int size) {
        try {
            Page<BookWithDiscountDto> books = iBookService.getBooksInStock(page, size);
            return ResponseEntity.ok(books);
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Page.empty());
        }
    }

    @PostMapping("/import-stock")
    public ResponseEntity<ResponseDto> importStock(@Valid @RequestBody ImportStockRequest request) {
        try {
            iBookService.importStock(request.getBooks());
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(new ResponseDto(BookConstants.STATUS_200, "Nhập kho thành công"));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ResponseDto(BookConstants.STATUS_500, "Lỗi khi nhập kho: " + e.getMessage()));
        }
    }

    @PostMapping("/details-by-ids")
    public ResponseEntity<List<BookDataForCartDto>> getBookDetailsByIds(@RequestBody List<String> bookIds) { // Sửa kiểu trả về
        try {
            // Service đã được cập nhật và sẽ trả về đúng kiểu
            List<BookDataForCartDto> details = iBookService.getBookDetailsByIds(bookIds); // Sửa kiểu biến
            return ResponseEntity.ok(details);
        } catch (Exception e) {
            System.err.println("Error fetching book details by IDs: " + e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.emptyList());
        }
    }

    @GetMapping("/all-details")
    public ResponseEntity<List<BookDetailDto>> getAllBookDetails() {
        try {
            List<BookDetailDto> bookDetails = iBookService.getAllBookDetails();
            return ResponseEntity.ok(bookDetails);
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.emptyList());
        }
    }

    @PostMapping("/internal/update-tags")
    public ResponseEntity<Void> updateBookTags(@RequestBody Map<String, List<String>> tagUpdates) {
        iBookService.updateTags(tagUpdates);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/by-ids")
    public ResponseEntity<List<Book>> getBooksByIds(@RequestBody List<String> bookIds) {
        // Bạn cần tạo hàm này trong BookServiceImpl
        // Nó chỉ cần gọi: bookRepository.findAllById(bookIds)
        List<Book> books = iBookService.findAllByIds(bookIds);
        return ResponseEntity.ok(books);
    }

    @GetMapping("/author/{authorName}")
    public ResponseEntity<Page<BookWithDiscountDto>> getBooksByAuthor(
            @PathVariable String authorName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Page<BookWithDiscountDto> books = iBookService.getBooksByAuthor(authorName, page, size);
            return ResponseEntity.ok(books);
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Page.empty());
        }
    }
}
