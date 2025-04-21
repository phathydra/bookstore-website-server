package com.bookstore.accounts.repository;

import com.bookstore.accounts.entity.Account;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountRepository extends MongoRepository<Account, String> {

    Optional<Account> findByEmail(String email);

    Optional<Account> findByEmailAndPassword(String email, String password);

    @Query("{'$or': [{'email': {$regex: ?0, $options: 'i'}}, {'role': {$regex: ?0, $options: 'i'}}]}")
    Page<Account> searchAccounts(String input, Pageable pageable);
}
