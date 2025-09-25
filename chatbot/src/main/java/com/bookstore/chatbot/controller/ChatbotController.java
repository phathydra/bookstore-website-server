package com.bookstore.chatbot.controller;


import com.bookstore.chatbot.service.ChatbotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/chatbot")
@CrossOrigin(origins = "http://localhost:3001")
public class ChatbotController {

    @Autowired
    private ChatbotService chatbotService;

    @PostMapping("")
    public ResponseEntity<String> chat(@RequestParam(name="message") String message) {
        String response = chatbotService.getChatbotResponse(message);
        return ResponseEntity.ok(response);
    }
}