package com.bookstore.chatbot.service;


import com.bookstore.chatbot.entity.Book;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class ChatbotService {
    private static final String API_URL = "https://openrouter.ai/api/v1/chat/completions";
    private static final String API_KEY = "YOUR_OPENROUTER_API_KEY"; // Replace with your actual API key

    // Dependencies
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate bookServiceRestTemplate = new RestTemplate(); // For calling the book service API

    /**
     * Processes a user message and returns the chatbot's response.
     *
     * @param userMessage The user's input message
     * @return The chatbot's response as a string
     */
    public String getChatbotResponse(String userMessage) {
        // Build the prompt for the AI
        String systemMessage = "You are a helpful assistant for a bookstore. Respond in JSON format with 'type' (either 'search', 'recommendation', or 'instruction') and 'parameters' or 'content' as appropriate. For example: " +
                "{ \"type\": \"search\", \"parameters\": { \"genre\": \"sci-fi\", \"author\": \"Asimov\" } } or " +
                "{ \"type\": \"instruction\", \"content\": \"Please specify a genre.\" }. Return only JSON, no extra text.";

        // Call the AI API
        String aiResponse = callAIAPI(systemMessage, userMessage);

        // Parse and process the AI response
        return processAiResponse(aiResponse);
    }

    /**
     * Sends the prompt to the OpenRouter.ai API and retrieves the raw response.
     */
    private String callAIAPI(String systemMessage, String userMessage) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + API_KEY);

        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("model", "mistralai/mistral-small-3.1-24b-instruct:free"); // Adjust model as needed
        requestBody.put("stream", false);

        ArrayNode messages = objectMapper.createArrayNode();

        // System message
        ObjectNode systemMsgNode = objectMapper.createObjectNode();
        systemMsgNode.put("role", "system");
        systemMsgNode.put("content", systemMessage);
        messages.add(systemMsgNode);

        // User message
        ObjectNode userMsgNode = objectMapper.createObjectNode();
        userMsgNode.put("role", "user");
        userMsgNode.put("content", userMessage);
        messages.add(userMsgNode);

        requestBody.set("messages", messages);
        requestBody.put("max_tokens", 500);

        try {
            String requestBodyStr = objectMapper.writeValueAsString(requestBody);
            HttpEntity<String> entity = new HttpEntity<>(requestBodyStr, headers);
            return restTemplate.postForObject(API_URL, entity, String.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to call AI API: " + e.getMessage());
        }
    }

    /**
     * Parses the AI response and processes it based on the 'type' field.
     */
    private String processAiResponse(String aiResponse) {
        try {
            // Parse the raw AI response
            JsonNode root = objectMapper.readTree(aiResponse);
            JsonNode contentNode = root.path("choices").get(0).path("message").path("content");
            if (contentNode.isMissingNode()) {
                throw new RuntimeException("No 'content' found in AI response.");
            }

            // Extract and clean the content
            String contentString = contentNode.asText().trim();
            contentString = contentString.replace("```json", "").replace("```", "").trim();

            // Parse the JSON content
            JsonNode json = objectMapper.readTree(contentString);
            String type = json.get("type").asText();

            // Process based on type
            switch (type) {
                case "search":
                case "recommendation":
                    JsonNode params = json.get("parameters");
                    String genre = params.has("genre") ? params.get("genre").asText() : "";
                    String author = params.has("author") ? params.get("author").asText() : "";
                    return fetchBooks(genre, author);
                case "instruction":
                    return json.get("content").asText();
                default:
                    return "Sorry, I couldn’t understand your request.";
            }
        } catch (Exception e) {
            return "Error processing response: " + e.getMessage();
        }
    }

    /**
     * Fetches books from the book service API based on genre and author.
     */
    private String fetchBooks(String genre, String author) {
        try {
            // Construct the book service URL
            StringBuilder bookServiceUrl = new StringBuilder("http://localhost:8081/api/books?");
            if (!genre.isEmpty()) {
                bookServiceUrl.append("genre=").append(genre);
            }
            if (!author.isEmpty()) {
                if (!genre.isEmpty()) bookServiceUrl.append("&");
                bookServiceUrl.append("author=").append(author);
            }

            // Call the book service
            Book[] books = bookServiceRestTemplate.getForObject(bookServiceUrl.toString(), Book[].class);
            if (books == null || books.length == 0) {
                return "No books found.";
            }

            // Format the book list
            StringBuilder response = new StringBuilder("Here are the books I found:\n");
            for (Book book : books) {
                response.append("- ").append(book.getTitle()).append(" by ").append(book.getAuthor()).append("\n");
            }
            return response.toString();
        } catch (Exception e) {
            return "Error fetching books: " + e.getMessage();
        }
    }
}