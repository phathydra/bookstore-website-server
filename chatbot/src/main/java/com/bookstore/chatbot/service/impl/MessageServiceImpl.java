package com.bookstore.chatbot.service.impl;

import com.bookstore.chatbot.dto.MessageDto;
import com.bookstore.chatbot.entity.AutoMessage;
import com.bookstore.chatbot.entity.Conversation;
import com.bookstore.chatbot.entity.Message;
import com.bookstore.chatbot.mapper.MessageMapper;
import com.bookstore.chatbot.repository.AutoMessageRepository;
import com.bookstore.chatbot.repository.ConversationRepository;
import com.bookstore.chatbot.repository.MessageRepository;
import com.bookstore.chatbot.service.IMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
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
    @Autowired
    private AutoMessageRepository autoMessageRepository;
    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

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
        messageDto.setCreatedAt(LocalDateTime.now());
        Message newMessage = messageRepository.save(MessageMapper.toMessage(messageDto, new Message()));
        autoReply(messageDto);
        return MessageMapper.toMessageDto(newMessage, new MessageDto());
    }

    private void autoReply(MessageDto messageDto){
        Optional<Conversation> conversation = conversationRepository.findById(messageDto.getConversationId());
        if(conversation.isPresent()){
            if (
                    messageDto.getSender().equals("CHATBOT") ||
                            messageDto.getSender().equals("ADMIN") ||
                            messageDto.getSender().equals(conversation.get().getAccountId1())
            ) {
                return;
            }
            Optional<AutoMessage> autoMessage;
            if(conversation.get().getChannelType().equals("Chatbot")){
                autoMessage = getAutoReply("CHATBOT");
            }
            else if(conversation.get().getChannelType().equals("Admin")){
                autoMessage = getAutoReply("ADMIN");
            }
            else{
                autoMessage = getAutoReply(conversation.get().getAccountId1());
            }

            if (autoMessage.isPresent()){
                Message message = new Message();
                message.setConversationId(conversation.get().getId());
                message.setContent(autoMessage.get().getContent());
                message.setSender(autoMessage.get().getAccountId());
                message.setCreatedAt(LocalDateTime.now());
                Message reply = messageRepository.save(message);
                simpMessagingTemplate.convertAndSend("/topic/conversation/" + conversation.get().getId(), reply);
            }
        }
    }

    private Optional<AutoMessage> getAutoReply(String accountId){
        return autoMessageRepository.findByAccountIdAndType(accountId, "REPLY");
    }
}
