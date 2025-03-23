package com.tlcn.books.repository;

import com.tlcn.books.entity.BookDiscount;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookDiscountRepository extends MongoRepository<BookDiscount, String> {
    Page<BookDiscount> findByBookId(Pageable pageable, String bookId);

    Page<BookDiscount> findByDiscountId(Pageable pageable, String discountId);

    void deleteByBookId(String bookId);

    void deleteByDiscountId(String discountId);
}
