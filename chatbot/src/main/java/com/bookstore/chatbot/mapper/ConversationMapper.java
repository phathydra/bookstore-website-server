package com.bookstore.chatbot.mapper;

import com.bookstore.chatbot.dto.ConversationDto;
import com.bookstore.chatbot.entity.Conversation;
import org.springframework.stereotype.Component;

@Component
public class ConversationMapper {
    public static Conversation toConversation(ConversationDto dto, Conversation conversation){
        conversation.setId(dto.getId());
        conversation.setUserId(dto.getUserId());
        conversation.setCreatedAt(dto.getCreatedAt());
        conversation.setLastUpdated(dto.getLastUpdated());
        return conversation;
    }

    public static ConversationDto toConversationDto(Conversation conversation, ConversationDto dto){
        dto.setId(conversation.getId());
        dto.setUserId(conversation.getUserId());
        dto.setCreatedAt(conversation.getCreatedAt());
        dto.setLastUpdated(conversation.getLastUpdated());
        return dto;
    }
}
