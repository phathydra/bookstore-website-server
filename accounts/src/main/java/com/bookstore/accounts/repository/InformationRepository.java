package com.bookstore.accounts.repository;

import com.bookstore.accounts.entity.Information;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InformationRepository extends MongoRepository<Information, String> {

    Optional<Information> findByAccountId(String accountId);

    void deleteByAccountId(String accountId);
}
