package com.bookstore.orders.repository;

import com.bookstore.orders.entity.RankVoucher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface RankVoucherRepository extends MongoRepository<RankVoucher, String> {
    List<RankVoucher> findAllByRank(int rank);

    Page<RankVoucher> findAllByCodeContainingIgnoreCaseOrderByEndDateDesc(Pageable pageable, String code);

    Page<RankVoucher> findByEndDateBefore(Date date, Pageable pageable);

    Page<RankVoucher> findByStartDateLessThanEqualAndEndDateGreaterThanEqual(
            Date startDate, Date endDate, Pageable pageable);

    Page<RankVoucher> findByStartDateAfter(Date date, Pageable pageable);

    Optional<RankVoucher> getRankVoucherByCode(String code);
}
