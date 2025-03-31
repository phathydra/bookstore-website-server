package com.bookstore.orders.repository;

import com.bookstore.orders.entity.Voucher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VoucherRepository extends MongoRepository<Voucher, String> {

    Page<Voucher> findAllBy(Pageable pageable);
    Optional<Voucher> getVoucherByCode(String code);
}
