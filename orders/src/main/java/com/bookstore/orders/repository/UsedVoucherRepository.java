package com.bookstore.orders.repository;

import com.bookstore.orders.entity.UsedVoucher;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsedVoucherRepository extends MongoRepository<UsedVoucher, String> {

    Optional<UsedVoucher> findByAccountId(String accountId);
}
