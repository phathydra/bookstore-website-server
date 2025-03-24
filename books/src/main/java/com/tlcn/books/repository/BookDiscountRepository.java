package com.tlcn.books.repository;

import com.tlcn.books.entity.BookDiscount;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookDiscountRepository extends MongoRepository<BookDiscount, String> {
    Optional<BookDiscount> findByBookIdAndDiscountId(String bookId, String discountId);

    Optional<BookDiscount> findByBookId(String bookId);

    List<BookDiscount> findByDiscountId(String discountId);

    void deleteByBookIdAndDiscountId(String bookId, String discountId);

    List<BookDiscount> findAll();
}
