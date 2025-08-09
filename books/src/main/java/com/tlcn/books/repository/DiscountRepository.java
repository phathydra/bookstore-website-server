package com.tlcn.books.repository;

import com.tlcn.books.entity.Discount;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DiscountRepository extends MongoRepository<Discount, String> {
    Page<Discount> findAllBy(Pageable pageable);
}
