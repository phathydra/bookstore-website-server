package com.tlcn.books.mapper;

import com.tlcn.books.dto.ReviewDTO;
import com.tlcn.books.entity.Review;

public class ReviewMapper {

    public static ReviewDTO toDTO(Review review) {
        return new ReviewDTO(
                review.getReviewId(),
                review.getBookId(),
                review.getAccountId(),
                review.getRating(),
                review.getComment(),
                review.getImage()
        );
    }

    public static Review toEntity(ReviewDTO dto) {
        return new Review(
                dto.getReviewId(),
                dto.getBookId(),
                dto.getAccountId(),
                dto.getRating(),
                dto.getComment(),
                dto.getImage()
        );
    }
}
