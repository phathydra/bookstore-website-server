package com.bookstore.chatbot.service;

import com.bookstore.chatbot.dto.MessageDto;
import org.springframework.stereotype.Service;

import java.util.List;

public interface IMessageService {
    List<MessageDto> loadALlMessagesByConversationId(String conversationId);

    MessageDto sendMessage(MessageDto messageDto);
}
