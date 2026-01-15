package com.bookstore.chatbot.repository;

import com.bookstore.chatbot.entity.FAQ;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FAQRepository extends MongoRepository<FAQ, String> {
}
