package com.bookstore.chatbot.service;

import com.bookstore.chatbot.config.ChatbotApiKeyConfig;
import com.bookstore.chatbot.entity.Book;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.bookstore.chatbot.dto.BookFilterInputDto;

import java.util.List;
import java.util.Map;

@Service
public class ChatbotService {
    private static final String API_URL = "https://openrouter.ai/api/v1/chat/completions";

    private final ChatbotApiKeyConfig API_KEY;

    public ChatbotService(ChatbotApiKeyConfig apikey){
        this.API_KEY = apikey;
    }

    // Dependencies
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate bookServiceRestTemplate = new RestTemplate();

    /**
     * Processes a user message and returns a formatted response from the chatbot.
     *
     * @param userMessage The user's input message
     * @return A formatted string response for the user
     */
    public String getChatbotResponse(String userMessage) {
        String allowedGenres = "Allowed genres include main categories and their subcategories: " +
                "Văn Học (Tiểu thuyết, Truyện ngắn, Thơ ca, Kịch, Ngụ ngôn), " +
                "Giáo Dục & Học Thuật (Sách giáo khoa, Sách tham khảo, Ngoại ngữ, Sách khoa học), " +
                "Kinh Doanh & Phát Triển Bản Thân (Quản trị, Tài chính, Khởi nghiệp, Lãnh đạo, Kỹ năng sống), " +
                "Khoa Học & Công Nghệ (Vật lý, Hóa học, Sinh học, Công nghệ, Lập trình), " +
                "Lịch Sử & Địa Lý (Lịch sử thế giới, Lịch sử Việt Nam, Địa lý), " +
                "Tôn Giáo & Triết Học (Phật giáo, Thiên Chúa giáo, Hồi giáo, Triết học), " +
                "Sách Thiếu Nhi (Truyện cổ tích, Truyện tranh, Sách giáo dục trẻ em), " +
                "Văn Hóa & Xã Hội (Du lịch, Nghệ thuật, Tâm lý - xã hội), " +
                "Sức Khỏe & Ẩm Thực (Nấu ăn, Dinh dưỡng, Thể dục - thể thao).";

        // Build the prompt for the AI
        String systemMessage =
                "You are a helpful assistant for a bookstore. Respond only in Vietnamese in JSON format with fields 'type' (either 'search', 'recommendation', or 'faq') and 'parameters' or 'content' as appropriate. " +
                        "Classify the user's request as follows: " +
                        "- Use 'search' if the user wants to find or look up specific books, including when they mention keywords like 'tìm', 'tìm kiếm', 'tra cứu', 'tôi muốn tìm', or provide details such as author, title, or genre. " +
                        "- Use 'recommendation' if the user asks for book suggestions, uses words like 'giới thiệu', 'đề xuất', 'gợi ý', 'nên đọc', or requests advice on what to read. " +
                        "- For all other cases that do not clearly match 'search' or 'recommendation', classify as 'faq'. " +
                        "Use only the following genres for the 'genre' field in your response: " + allowedGenres + ". " +
                        "If the user mentions a genre that is misspelled or similar to one of the allowed genres, map it to the closest valid match. " +
                        "Output examples: " +
                        "{ \"type\": \"search\", \"parameters\": { \"genre\": \"Văn Học\", \"author\": \"Nguyễn Nhật Ánh\" } } " +
                        "{ \"type\": \"recommendation\", \"parameters\": { \"genre\": \"Vật lý\" } } " +
                        "{ \"type\": \"faq\", \"parameters\": \"{\"content\": \"plain message of input, no change here\"}\" }. " +
                        "Return only valid JSON — do not include any extra explanation or text.";

        // Call the AI API
        String aiResponse = callAIAPI(systemMessage, userMessage);

        // Extract and process the content
        try {
            JsonNode root = objectMapper.readTree(aiResponse);
            JsonNode contentNode = root.path("choices").get(0).path("message").path("content");
            if (contentNode.isMissingNode()) {
                return "Xin lỗi quý khách, tôi không thể xử lý yêu cầu của bạn lúc này. Vui lòng thử lại sau.";
            }
            String contentString = contentNode.asText().trim();
            contentString = contentString.replace("```json", "").replace("```", "").trim();
            return processAiResponse(contentString);
        } catch (Exception e) {
            return "Xin lỗi quý khách, có lỗi xảy ra khi xử lý yêu cầu của bạn: " + e.getMessage() + ". Vui lòng thử lại.";
        }
    }

    /**
     * Sends the prompt to the OpenRouter.ai API and retrieves the raw response.
     */
    private String callAIAPI(String systemMessage, String userMessage) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + API_KEY.getApikey());

        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("model", "mistralai/mistral-small-3.1-24b-instruct:free");
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
            throw new RuntimeException("Không thể kết nối đến dịch vụ AI: " + e.getMessage());
        }
    }

    /**
     * Parses the AI response and processes it based on the 'type' field.
     */
    private String processAiResponse(String aiResponse) {
        try {
            JsonNode json = objectMapper.readTree(aiResponse);
            String type = json.get("type").asText();

            switch (type) {
                case "search":

                case "recommendation":
                    JsonNode params = json.get("parameters");
                    String genre = params.has("genre") ? params.get("genre").asText() : "";
                    String author = params.has("author") ? params.get("author").asText() : "";
                    return fetchBooks(genre, author, type);
                case "faq":
                    String content = json.get("parameters").get("content").asText();
                    return faqRAGFetch(content);
                default:
                    return "Xin lỗi quý khách, tôi chưa hiểu rõ yêu cầu của bạn. Vui lòng cung cấp thêm thông tin để tôi có thể hỗ trợ tốt hơn.";
            }
        } catch (Exception e) {
            return "Xin lỗi quý khách, có lỗi xảy ra khi xử lý phản hồi: " + e.getMessage() + ". Vui lòng thử lại sau.";
        }
    }

    /**
     * Fetches books from the BookController and formats the response with links.
     */
    private String fetchBooks(String genre, String author, String type) {
        try {
            BookFilterInputDto filterDto = new BookFilterInputDto();
            if (!author.isEmpty()) {
                filterDto.setBookAuthor(author);
            }
            if (!genre.isEmpty()) {
                filterDto.setMainCategory(List.of(genre));
            }

            String url = "http://localhost:8081/api/book/filter?page=0&size=5";

            ResponseEntity<String> response = bookServiceRestTemplate.postForEntity(url, filterDto, String.class);
            JsonNode jsonResponse = objectMapper.readTree(response.getBody());
            JsonNode contentArray = jsonResponse.get("content");

            if (contentArray == null || !contentArray.isArray() || contentArray.size() == 0) {
                return "Rất tiếc, tôi không tìm thấy sách nào phù hợp với yêu cầu của quý khách.";
            }

            // Format the response based on type
            StringBuilder formattedResponse = new StringBuilder(
                    type.equals("search")
                            ? "Dưới đây là kết quả tìm kiếm của quý khách:<br />"
                            : "Dưới đây là những cuốn sách tôi đề xuất cho quý khách:<br />"
            );
            for (JsonNode bookNode : contentArray) {
                String bookId = bookNode.get("bookId").asText();
                String title = bookNode.get("bookName").asText();
                String bookAuthor = bookNode.get("bookAuthor").asText();
                String bookLink = "<a href=\"http://localhost:3001/productdetail/" + bookId + "\">" + title + "</a>";
                formattedResponse.append("- ").append(bookLink).append(" của ").append(bookAuthor).append("<br />");
            }

            formattedResponse.append("Quý khách có thể nhấp vào tên sách để xem chi tiết. Chúc quý khách tìm được cuốn sách ưng ý!");
            return formattedResponse.toString();
        } catch (Exception e) {
            return "Xin lỗi quý khách, có lỗi xảy ra khi tìm kiếm sách: " + e.getMessage() + ". Vui lòng thử lại sau.";
        }
    }

    private String faqRAGFetch(String userMssg) {
        try {
            String url = "http://localhost:8085/ask";

            Map<String, String> body = Map.of("question", userMssg);

            ResponseEntity<String> response = bookServiceRestTemplate.postForEntity(url, body, String.class);

            JsonNode jsonResponse = objectMapper.readTree(response.getBody());
            String answer = jsonResponse.has("answer")
                    ? jsonResponse.get("answer").asText()
                    : "Không tìm thấy câu trả lời phù hợp cho câu hỏi của quý khách.";
            
            StringBuilder formattedResponse = new StringBuilder();
            formattedResponse.append("Tôi đã tìm thấy thông tin sau cho câu hỏi của quý khách:<br />");
            formattedResponse.append(answer);
            formattedResponse.append("<br /><br />Cảm ơn quý khách đã liên hệ với chúng tôi!");

            return formattedResponse.toString();

        } catch (Exception e) {
            return "Xin lỗi quý khách, có lỗi xảy ra khi truy vấn thông tin FAQ: " + e.getMessage() + ". Vui lòng thử lại sau.";
        }
    }

}