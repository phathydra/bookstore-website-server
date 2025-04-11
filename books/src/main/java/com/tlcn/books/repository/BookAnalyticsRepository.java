package com.tlcn.books.repository;

import com.tlcn.books.entity.BookAnalytics;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BookAnalyticsRepository extends MongoRepository<BookAnalytics, String> {
    Optional<BookAnalytics> findByBookId(String bookId);
}
