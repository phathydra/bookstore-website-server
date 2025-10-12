package com.bookstore.chatbot.mapper;

import com.bookstore.chatbot.dto.ConversationDto;
import com.bookstore.chatbot.entity.Conversation;
import org.springframework.stereotype.Component;

@Component
public class ConversationMapper {
    public static Conversation toConversation(ConversationDto dto, Conversation conversation){
        conversation.setId(dto.getId());
        conversation.setTitle(dto.getTitle());
        conversation.setUserId1(dto.getUserId1());
        conversation.setUserId2(dto.getUserId2());
        conversation.setChannelType(dto.getChannelType());
        conversation.setCreatedAt(dto.getCreatedAt());
        conversation.setLastUpdated(dto.getLastUpdated());
        return conversation;
    }

    public static ConversationDto toConversationDto(Conversation conversation, ConversationDto dto){
        dto.setId(conversation.getId());
        dto.setTitle(conversation.getTitle());
        dto.setUserId1(conversation.getUserId1());
        dto.setUserId2(conversation.getUserId2());
        dto.setChannelType(conversation.getChannelType());
        dto.setCreatedAt(conversation.getCreatedAt());
        dto.setLastUpdated(conversation.getLastUpdated());
        return dto;
    }
}
