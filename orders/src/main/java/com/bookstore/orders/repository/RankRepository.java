package com.bookstore.orders.repository;

import com.bookstore.orders.dto.RankDto;
import com.bookstore.orders.entity.Rank;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RankRepository extends MongoRepository<Rank, String> {
    Optional<Rank> findByAccountId(String accountId);
}
