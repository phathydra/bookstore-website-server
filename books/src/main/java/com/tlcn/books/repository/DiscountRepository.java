package com.tlcn.books.repository;

import com.tlcn.books.entity.Discount;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@Repository
public interface DiscountRepository extends MongoRepository<Discount, String> {
    Page<Discount> findAllBy(Pageable pageable);

    Page<Discount> findByEndDateBefore(Date now, Pageable pageable);

    Page<Discount> findByStartDateLessThanEqualAndEndDateGreaterThanEqual(Date now1, Date now2, Pageable pageable);

    Page<Discount> findByStartDateAfter(Date now, Pageable pageable);

    List<Discount> findByEndDateBefore(Date date);
    Page<Discount> findByTypeAndStartDateLessThanEqualAndEndDateGreaterThanEqual(String type, Date now1, Date now2, Pageable pageable);

    // Trong interface DiscountRepository
    List<Discount> findByTypeAndStartDateLessThanEqualAndEndDateGreaterThanEqual(String type, Date now1, Date now2);

}
