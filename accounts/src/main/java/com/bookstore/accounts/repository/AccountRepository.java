package com.bookstore.accounts.repository;

import com.bookstore.accounts.entity.Account;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.mongodb.repository.Query;

@Repository
public interface AccountRepository extends MongoRepository<Account, String> {

    Optional<Account> findByUsername(String username);

    Optional<Account> findByUsernameAndPassword(String username, String password);

    @Query("{'$or': [{'username': {$regex: ?0, $options: 'i'}}, {'email': {$regex: ?0, $options: 'i'}}]}")
    Page<Account> searchAccounts(String input, Pageable pageable);
}
