package com.bookstore.chatbot.repository;

import com.bookstore.chatbot.entity.AutoMessage;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AutoMessageRepository extends MongoRepository<AutoMessage, String> {
    Optional<AutoMessage> findByAccountIdAndType(String accountId, String type);
}
