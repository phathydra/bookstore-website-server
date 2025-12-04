package com.bookstore.orders.repository;

import com.bookstore.orders.entity.MonthlyPoints;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MonthlyPointsRepository extends MongoRepository<MonthlyPoints, String> {
    Optional<MonthlyPoints> findByAccountIdAndMonthAndYear(String accountId, int month, int year);

    List<MonthlyPoints> findTop12ByAccountIdOrderByYearDescMonthDesc(String accountId);
}
