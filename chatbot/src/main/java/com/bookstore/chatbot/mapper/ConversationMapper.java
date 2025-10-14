package com.bookstore.chatbot.mapper;

import com.bookstore.chatbot.dto.ConversationDto;
import com.bookstore.chatbot.entity.Conversation;
import org.springframework.stereotype.Component;

@Component
public class ConversationMapper {
    public static Conversation toConversation(ConversationDto dto, Conversation conversation){
        conversation.setId(dto.getId());
        conversation.setTitle1(dto.getTitle1());
        conversation.setTitle2(dto.getTitle2());
        conversation.setAccountId1(dto.getAccountId1());
        conversation.setAccountId2(dto.getAccountId2());
        conversation.setChannelType(dto.getChannelType());
        conversation.setCreatedAt(dto.getCreatedAt());
        conversation.setLastUpdated(dto.getLastUpdated());
        return conversation;
    }

    public static ConversationDto toConversationDto(Conversation conversation, ConversationDto dto){
        dto.setId(conversation.getId());
        dto.setTitle1(conversation.getTitle1());
        dto.setTitle2(conversation.getTitle2());
        dto.setAccountId1(conversation.getAccountId1());
        dto.setAccountId2(conversation.getAccountId2());
        dto.setChannelType(conversation.getChannelType());
        dto.setCreatedAt(conversation.getCreatedAt());
        dto.setLastUpdated(conversation.getLastUpdated());
        return dto;
    }
}
