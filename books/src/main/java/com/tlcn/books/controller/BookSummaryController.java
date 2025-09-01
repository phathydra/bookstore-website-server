package com.tlcn.books.controller;

import com.tlcn.books.dto.BookSummaryResponseDto;
import com.tlcn.books.service.IBookSummaryService;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"}, exposedHeaders = "Content-Disposition")
@RestController
@RequestMapping("/api/summary")
public class BookSummaryController {

    private final IBookSummaryService bookSummaryService;

    public BookSummaryController(IBookSummaryService bookSummaryService) {
        this.bookSummaryService = bookSummaryService;
    }

    @GetMapping
    public BookSummaryResponseDto summarizeBook(
            @RequestParam String title,
            @RequestParam String author) {
        return bookSummaryService.getBookSummary(title, author);
    }
}
