package com.bookstore.orders.repository;

import com.bookstore.orders.entity.ObtainableVoucher;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ObtainableVoucherRepository extends MongoRepository<ObtainableVoucher, String> {
    Optional<ObtainableVoucher> getObtainableVoucherByCode(String code);
}
