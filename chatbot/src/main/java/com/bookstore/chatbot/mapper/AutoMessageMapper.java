package com.bookstore.chatbot.mapper;

import com.bookstore.chatbot.dto.AutoMessageDto;
import com.bookstore.chatbot.entity.AutoMessage;
import org.springframework.stereotype.Component;

@Component
public class AutoMessageMapper {
    public static AutoMessage toAutoMessage(AutoMessageDto dto, AutoMessage autoMessage) {
        autoMessage.setId(dto.getId());
        autoMessage.setAccountId(dto.getAccountId());
        autoMessage.setType(dto.getType());
        autoMessage.setContent(dto.getContent());
        autoMessage.setMetadata(dto.getMetadata());
        return autoMessage;
    }

    public static AutoMessageDto toAutoMessageDto(AutoMessage autoMessage, AutoMessageDto dto) {
        dto.setId(autoMessage.getId());
        dto.setAccountId(autoMessage.getAccountId());
        dto.setType(autoMessage.getType());
        dto.setContent(autoMessage.getContent());
        dto.setMetadata(autoMessage.getMetadata());
        return dto;
    }
}
