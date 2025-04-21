package com.bookstore.accounts.repository;

import com.bookstore.accounts.entity.Email;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface EmailRepository extends MongoRepository<Email, String> {
}