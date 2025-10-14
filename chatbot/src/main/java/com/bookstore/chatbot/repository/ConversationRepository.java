package com.bookstore.chatbot.repository;

import com.bookstore.chatbot.entity.Conversation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConversationRepository extends MongoRepository<Conversation, String> {
    List<Conversation> findAllByAccountId1OrAccountId2OrderByLastUpdatedDesc(String accountId1, String accountId2);
}
