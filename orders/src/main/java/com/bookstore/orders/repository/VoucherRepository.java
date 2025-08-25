package com.bookstore.orders.repository;

import com.bookstore.orders.entity.Voucher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.Optional;
import java.util.List;

@Repository
public interface VoucherRepository extends MongoRepository<Voucher, String> {

    Page<Voucher> findAllByCodeContainingIgnoreCaseOrderByEndDateDesc(Pageable pageable, String code);
    Optional<Voucher> getVoucherByCode(String code);

    List<Voucher> getAllByPublish(boolean publish);

    Page<Voucher> findByEndDateBefore(Date now, Pageable pageable);

    Page<Voucher> findByStartDateLessThanEqualAndEndDateGreaterThanEqual(Date now1, Date now2, Pageable pageable);

    Page<Voucher> findByStartDateAfter(Date now, Pageable pageable);
}
