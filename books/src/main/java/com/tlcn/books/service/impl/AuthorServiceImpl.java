package com.tlcn.books.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tlcn.books.dto.AuthorResponseDto;
import com.tlcn.books.service.IAuthorService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

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
                "Lưu ý: URL ảnh (imageUrl) phải là một link trực tiếp tới ảnh của tác giả có thể nhấp vào link và xem được.";

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

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(
                    apiUrl + "/chat/completions",
                    request,
                    String.class
            );

            JsonNode root = objectMapper.readTree(response.getBody());

            // ✅ OpenRouter trả về khác OpenAI, nên cần lấy đúng path
            String content = root.path("choices").get(0)
                    .path("message")
                    .path("content").asText();

            return objectMapper.readValue(content, AuthorResponseDto.class);

        } catch (Exception e) {
            throw new RuntimeException("Error calling OpenRouter API: " + e.getMessage(), e);
        }
    }
}
