package com.bookstore.accounts.repository;

import com.bookstore.accounts.entity.Account;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountRepository extends MongoRepository<Account, String> {

    Optional<Account> findByUsername(String username);

    Account findByUsernameAndPassword(String username, String password);
}
