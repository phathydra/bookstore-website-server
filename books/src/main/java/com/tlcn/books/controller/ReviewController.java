package com.tlcn.books.controller;

import com.tlcn.books.dto.ReviewDTO;
import com.tlcn.books.service.IReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    @Autowired
    private IReviewService reviewService;

    @PostMapping
    public ResponseEntity<ReviewDTO> createReview(@RequestBody ReviewDTO reviewDTO) {
        return ResponseEntity.ok(reviewService.createReview(reviewDTO));
    }

    @GetMapping("/book/{bookId}")
    public ResponseEntity<List<ReviewDTO>> getReviewsByBookId(@PathVariable String bookId) {
        return ResponseEntity.ok(reviewService.getReviewsByBookId(bookId));
    }

    @GetMapping("/account/{accountId}")
    public ResponseEntity<List<ReviewDTO>> getReviewsByAccountId(@PathVariable String accountId) {
        return ResponseEntity.ok(reviewService.getReviewsByAccountId(accountId));
    }
}
