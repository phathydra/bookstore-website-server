package com.bookstore.chatbot.service.impl;

import com.bookstore.chatbot.dto.ConversationDto;
import com.bookstore.chatbot.entity.AutoMessage;
import com.bookstore.chatbot.entity.Conversation;
import com.bookstore.chatbot.entity.Message;
import com.bookstore.chatbot.mapper.ConversationMapper;
import com.bookstore.chatbot.repository.AutoMessageRepository;
import com.bookstore.chatbot.repository.ConversationRepository;
import com.bookstore.chatbot.repository.MessageRepository;
import com.bookstore.chatbot.service.IConversationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ConversationServiceImpl implements IConversationService {
    @Autowired
    private ConversationRepository conversationRepository;
    @Autowired
    private MessageRepository messageRepository;
    @Autowired
    private AutoMessageRepository autoMessageRepository;

    @Override
    public List<ConversationDto> getAllConversationByUserId(String accountId) {
        List<Conversation> conversations = conversationRepository.findAllByAccountId1OrAccountId2OrderByLastUpdatedDesc(accountId, accountId);
        return conversations.stream().map(conversation -> ConversationMapper.toConversationDto(conversation, new ConversationDto())).toList();
    }

    @Override
    public List<ConversationDto> getAllConversationByUserIdAdmin(String accountId) {
        List<Conversation> conversations = conversationRepository.findAllByAccountId1OrAccountId2OrderByLastUpdatedDesc(accountId, accountId);
        List<Conversation> conversationsAdmin = conversationRepository.findAllByAccountId1OrAccountId2OrderByLastUpdatedDesc("Admin", "Admin");
        conversations.addAll(conversationsAdmin);
        return conversations.stream().map(conversation -> ConversationMapper.toConversationDto(conversation, new ConversationDto())).toList();
    }

    @Override
    public ConversationDto getInfo(String conversationId) {
        Optional<Conversation> conversation = conversationRepository.findById(conversationId);
        return ConversationMapper.toConversationDto(conversation.get(), new ConversationDto());
    }

    @Override
    public ConversationDto createNewConversation(ConversationDto conversationDto) {
        conversationDto.setCreatedAt(LocalDateTime.now());
        conversationDto.setLastUpdated(LocalDateTime.now());
        Conversation newConversation = conversationRepository.save(ConversationMapper.toConversation(conversationDto, new Conversation()));
        autoGreeting(newConversation);
        return ConversationMapper.toConversationDto(newConversation, new ConversationDto());
    }

    private void autoGreeting(Conversation conversation){
        Optional<AutoMessage> autoMessage;
        if(conversation.getChannelType().equals("Chatbot")){
            autoMessage = getAutoGreeting("CHATBOT");
        }
        else if (conversation.getChannelType().equals("Admin")) {
            autoMessage = getAutoGreeting("ADMIN");
        }
        else {
            autoMessage = getAutoGreeting(conversation.getAccountId1());
        }
        if (autoMessage.isPresent()){
            Message message = new Message();
            message.setConversationId(conversation.getId());
            message.setContent(autoMessage.get().getContent());
            message.setSender(autoMessage.get().getAccountId());
            message.setCreatedAt(LocalDateTime.now());
            messageRepository.save(message);
        }
    }

    private Optional<AutoMessage> getAutoGreeting(String accountId){
        return autoMessageRepository.findByAccountIdAndType(accountId, "GREETING");
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
