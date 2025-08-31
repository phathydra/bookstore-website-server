package com.bookstore.orders.repository;

import com.bookstore.orders.entity.ObtainableVoucher;
import com.bookstore.orders.entity.ObtainedVoucher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface ObtainableVoucherRepository extends MongoRepository<ObtainableVoucher, String> {
    Optional<ObtainableVoucher> getObtainableVoucherByCode(String code);

    Optional<ObtainableVoucher> findByCodeAndEndDateGreaterThanEqual(String code, Date now);

    Page<ObtainableVoucher> findAllByOrderByEndDateDesc(Pageable pageable);

    List<ObtainableVoucher> getObtainableVoucherByPublicClaimable(boolean publicClaimable);
}
