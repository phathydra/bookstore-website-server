package com.tlcn.books.service.impl;

import com.tlcn.books.dto.ReviewDTO;
import com.tlcn.books.entity.Review;
import com.tlcn.books.mapper.ReviewMapper;
import com.tlcn.books.repository.ReviewRepository;
import com.tlcn.books.service.IReviewService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReviewServiceImpl implements IReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    @Override
    public ReviewDTO createReview(ReviewDTO reviewDTO) {
        Review review = ReviewMapper.toEntity(reviewDTO);
        Review saved = reviewRepository.save(review);
        return ReviewMapper.toDTO(saved);
    }

    @Override
    public List<ReviewDTO> getReviewsByBookId(String bookId) {
        return reviewRepository.findByBookId(bookId)
                .stream()
                .map(ReviewMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ReviewDTO> getReviewsByAccountId(String accountId) {
        return reviewRepository.findByAccountId(accountId)
                .stream()
                .map(ReviewMapper::toDTO)
                .collect(Collectors.toList());
    }
}
