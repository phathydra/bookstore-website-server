package com.bookstore.chatbot.repository;

import com.bookstore.chatbot.entity.Message;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends MongoRepository<Message, String> {
    List<Message> findAllByConversationIdOrderByCreatedAtAsc(String conversationId);
}
