package com.bookstore.chatbot.mapper;

import com.bookstore.chatbot.dto.MessageDto;
import com.bookstore.chatbot.entity.Message;
import org.springframework.stereotype.Component;

@Component
public class MessageMapper {
    public static Message toMessage(MessageDto dto, Message message){
        message.setConversationId(dto.getConversationId());
        message.setContent(dto.getContent());
        message.setSender(dto.getSender());
        message.setCreatedAt(dto.getCreatedAt());
        return message;
    }

    public static MessageDto toMessageDto(Message message, MessageDto dto){
        dto.setConversationId(message.getConversationId());
        dto.setContent(message.getContent());
        dto.setSender(message.getSender());
        dto.setCreatedAt(message.getCreatedAt());
        return dto;
    }
}
