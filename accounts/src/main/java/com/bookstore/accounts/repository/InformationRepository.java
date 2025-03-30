package com.bookstore.accounts.repository;

import com.bookstore.accounts.entity.Information;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InformationRepository extends MongoRepository<Information, String> {

    Optional<Information> findByAccountId(String accountId);

    void deleteByAccountId(String accountId);

    @Query("{'$or': [{'accountId': {$regex: ?0, $options: 'i'}}, {'name': {$regex: ?0, $options: 'i'}}, {'email': {$regex: ?0, $options: 'i'}}, {'phone': {$regex: ?0, $options: 'i'}}]}")
    Page<Information> searchInformation(String input, Pageable pageable);

}
