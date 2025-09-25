package com.bookstore.chatbot.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ChatbotApiKeyConfig {

    @Value("${app.api.openrouter-key}")
    private String apikey;

    public String getApikey(){
        return apikey;
    }
}
