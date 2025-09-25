package com.bookstore.chatbot.service.impl;

import com.bookstore.chatbot.dto.ConversationDto;
import com.bookstore.chatbot.entity.Conversation;
import com.bookstore.chatbot.entity.Message;
import com.bookstore.chatbot.mapper.ConversationMapper;
import com.bookstore.chatbot.repository.ConversationRepository;
import com.bookstore.chatbot.repository.MessageRepository;
import com.bookstore.chatbot.service.IConversationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ConversationServiceImpl implements IConversationService {
    @Autowired
    private ConversationRepository conversationRepository;
    @Autowired
    private MessageRepository messageRepository;

    @Override
    public List<ConversationDto> getAllConversationByUserId(String userId) {
        List<Conversation> conversations = conversationRepository.findAllByUserIdOrderByLastUpdatedDesc(userId);
        return conversations.stream().map(conversation -> ConversationMapper.toConversationDto(conversation, new ConversationDto())).toList();
    }

    @Override
    public ConversationDto createNewConversation(ConversationDto conversationDto) {
        conversationDto.setCreatedAt(LocalDateTime.now());
        conversationDto.setLastUpdated(LocalDateTime.now());
        Conversation newConversation = conversationRepository.save(ConversationMapper.toConversation(conversationDto, new Conversation()));
        return ConversationMapper.toConversationDto(newConversation, new ConversationDto());
    }

    @Override
    public ConversationDto updateConversation(ConversationDto conversationDto) {
        Conversation newConversation = conversationRepository.save(ConversationMapper.toConversation(conversationDto, new Conversation()));
        return ConversationMapper.toConversationDto(newConversation, new ConversationDto());
    }

    @Override
    public void deleteConversation(String conversationId) {
        List<Message> messages = messageRepository.findAllByConversationIdOrderByCreatedAtAsc(conversationId);
        messageRepository.deleteAll(messages);
        conversationRepository.deleteById(conversationId);
    }
}
