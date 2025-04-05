package com.tlcn.books.service;

import com.tlcn.books.dto.ReviewDTO;

import java.util.List;

public interface IReviewService {

    ReviewDTO createReview(ReviewDTO reviewDTO);

    List<ReviewDTO> getReviewsByBookId(String bookId);

    List<ReviewDTO> getReviewsByAccountId(String accountId);
}
