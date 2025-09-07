package com.tlcn.books.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tlcn.books.dto.AuthorResponseDto;
import com.tlcn.books.service.IAuthorService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;


import java.util.List;
import java.util.Map;

@Service
public class AuthorServiceImpl implements IAuthorService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${openrouter.api.key}")
    private String apiKey;

    @Value("${openrouter.api.url}")
    private String apiUrl;

    @Override
    public AuthorResponseDto getAuthorInfo(String authorName) {
        try {
            // Encode để an toàn khi gửi qua API ngoài
            String safeName = URLEncoder.encode(authorName, StandardCharsets.UTF_8.toString());

            String prompt = "Hãy trả về thông tin chi tiết về tác giả " + authorName +
                    " dưới dạng JSON hợp lệ, KHÔNG thêm giải thích, KHÔNG thêm text ngoài JSON." +
                    " JSON phải có cấu trúc như sau: " +
                    "{ " +
                    "\"name\": \"...\", " +
                    "\"birthDate\": \"YYYY-MM-DD\", " +
                    "\"birthPlace\": \"...\", " +
                    "\"occupation\": [\"...\"], " +
                    "\"genre\": [\"...\"], " +
                    "\"biography\": \"...\", " +
                    "\"imageUrl\": \"...\", " + // Add specific instruction here
                    "\"notableWorks\": [{\"title\": \"...\", \"type\": \"...\", \"year\": 0}], " +
                    "\"awards\": [{\"name\": \"...\", \"year\": 0, \"work\": \"...\"}], " +
                    "\"externalLinks\": {\"wikipedia\": \"...\", \"goodreads\": \"...\"} " +
                    "}" +
                    "Lưu ý: Trường imageUrl PHẢI là link ảnh trực tiếp (.jpg, .png, .jpeg) có thể mở trong trình duyệt và load trong thẻ <img>. Không được trả về link Wikipedia, Google Image hay trang web." +
                    "ví dụ hình ảnh kiểu https://upload.wikimedia.org/wikipedia/vi/8/80/Xuan_Quynh.jpg"
                    ;

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiKey);

            Map<String, Object> body = Map.of(
                    "model", "gpt-4o-mini",
                    "messages", List.of(
                            Map.of("role", "user", "content", prompt)
                    )
            );

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(
                    apiUrl + "/chat/completions",
                    request,
                    String.class
            );

            JsonNode root = objectMapper.readTree(response.getBody());

            String content = root.path("choices").get(0)
                    .path("message")
                    .path("content").asText();

            return objectMapper.readValue(content, AuthorResponseDto.class);

        } catch (Exception e) {
            throw new RuntimeException("Error calling OpenRouter API: " + e.getMessage(), e);
        }
    }
}
