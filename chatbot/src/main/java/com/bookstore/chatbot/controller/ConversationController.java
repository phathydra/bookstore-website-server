package com.bookstore.chatbot.controller;

import com.bookstore.chatbot.dto.ConversationDto;
import com.bookstore.chatbot.service.IConversationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/conversation")
@CrossOrigin(origins = {
        "http://localhost:3000",
        "http://localhost:3001",
        "http://localhost:3002"
})
public class ConversationController {
    @Autowired
    private IConversationService iConversationService;

    @GetMapping("/fetch")
    public ResponseEntity<List<ConversationDto>> fetchConversations(@RequestParam("accountId") String accountId){
        List<ConversationDto> conversations = iConversationService.getAllConversationByUserId(accountId);
        if(conversations.isEmpty()){
            return ResponseEntity.ok(Collections.emptyList());
        }
        return ResponseEntity.ok(conversations);
    }

    @GetMapping("/fetch-admin")
    public ResponseEntity<List<ConversationDto>> fetchConversationsAdmin(@RequestParam("accountId") String accountId){
        List<ConversationDto> conversations = iConversationService.getAllConversationByUserIdAdmin(accountId);
        if(conversations.isEmpty()){
            return ResponseEntity.ok(Collections.emptyList());
        }
        return ResponseEntity.ok(conversations);
    }

    @GetMapping("/info")
    public ResponseEntity<ConversationDto> getInfo(@RequestParam("conversationId") String conversationId){
        ConversationDto conversationDto = iConversationService.getInfo(conversationId);
        return ResponseEntity.ok(conversationDto);
    }

    @PostMapping("/create")
    public ResponseEntity<ConversationDto> createConversation(@RequestBody ConversationDto conversationDto){
        ConversationDto newConversation = iConversationService.createNewConversation(conversationDto);
        return ResponseEntity.ok(newConversation);
    }

    @PutMapping("/update")
    public ResponseEntity<ConversationDto> updateConversation(@RequestBody ConversationDto conversationDto){
        ConversationDto updatedConversation = iConversationService.updateConversation(conversationDto);
        return ResponseEntity.ok(updatedConversation);
    }

    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteConversation(@RequestParam String conversationId){
        iConversationService.deleteConversation(conversationId);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body("Delete conversation successfully!");
    }
}
