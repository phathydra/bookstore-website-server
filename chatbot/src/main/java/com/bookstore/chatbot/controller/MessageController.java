package com.bookstore.chatbot.controller;

import com.bookstore.chatbot.dto.MessageDto;
import com.bookstore.chatbot.service.IMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/message")
@CrossOrigin(origins = "http://localhost:3001")
public class MessageController {
    @Autowired
    private IMessageService iMessageService;

    @GetMapping("/fecth")
    public ResponseEntity<List<MessageDto>> loadAllMessages(@RequestParam("conversationId") String conversationId){
        List<MessageDto> messageDtos = iMessageService.loadALlMessagesByConversationId(conversationId);
        return ResponseEntity.ok(messageDtos);
    }

    @PostMapping("/send")
    public ResponseEntity<MessageDto> sendMessage(@RequestBody MessageDto messageDto){
        MessageDto newMessage = iMessageService.sendMessage(messageDto);
        return ResponseEntity.ok(newMessage);
    }
}
