package com.bookstore.chatbot.service.impl;

import com.bookstore.chatbot.dto.MessageDto;
import com.bookstore.chatbot.entity.Conversation;
import com.bookstore.chatbot.entity.Message;
import com.bookstore.chatbot.mapper.MessageMapper;
import com.bookstore.chatbot.repository.ConversationRepository;
import com.bookstore.chatbot.repository.MessageRepository;
import com.bookstore.chatbot.service.IMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class MessageServiceImpl implements IMessageService {
    @Autowired
    private MessageRepository messageRepository;
    @Autowired
    private ConversationRepository conversationRepository;

    @Override
    public List<MessageDto> loadALlMessagesByConversationId(String conversationId) {
        List<Message> messages = messageRepository.findAllByConversationIdOrderByCreatedAtAsc(conversationId);
        return messages.stream().map(message -> MessageMapper.toMessageDto(message, new MessageDto())).toList();
    }

    @Override
    public MessageDto sendMessage(MessageDto messageDto) {
        Optional<Conversation> conversation = conversationRepository.findById(messageDto.getConversationId());
        if(conversation.isPresent()){
            conversation.get().setLastUpdated(LocalDateTime.now());
            conversationRepository.save(conversation.get());
        }
        Message newMessage = messageRepository.save(MessageMapper.toMessage(messageDto, new Message()));
        return MessageMapper.toMessageDto(newMessage, new MessageDto());
    }
}
