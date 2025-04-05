package com.tlcn.books.repository;

import com.tlcn.books.entity.Review;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ReviewRepository extends MongoRepository<Review, String> {

    List<Review> findByBookId(String bookId);

    List<Review> findByAccountId(String accountId);
}
