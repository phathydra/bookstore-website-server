package com.bookstore.orders.repository;

import com.bookstore.orders.entity.ObtainedVoucher;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ObtainedVoucherRepository extends MongoRepository<ObtainedVoucher, String> {

    Optional<ObtainedVoucher> findByCode(String code);
    List<ObtainedVoucher> getObtainedVoucherByAccountId(String accountId);

    Optional<ObtainedVoucher> findByAccountIdAndCode(String accountId, String code);

    void deleteByAccountIdAndCode(String accountId, String code);
}
