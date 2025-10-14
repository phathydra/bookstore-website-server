package com.bookstore.chatbot.service;

import com.bookstore.chatbot.dto.ConversationDto;
import org.springframework.stereotype.Service;

import java.util.List;

public interface IConversationService {
    List<ConversationDto> getAllConversationByUserId(String accountId);
    List<ConversationDto> getAllConversationByUserIdAdmin(String accountId);
    ConversationDto getInfo(String conversationId);
    ConversationDto createNewConversation(ConversationDto conversationDto);
    ConversationDto updateConversation(ConversationDto conversationDto);
    void deleteConversation(String conversationId);
}
