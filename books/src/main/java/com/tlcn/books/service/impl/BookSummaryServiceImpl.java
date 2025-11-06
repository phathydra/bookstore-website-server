package com.tlcn.books.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tlcn.books.dto.BookSummaryResponseDto;
import com.tlcn.books.service.IBookSummaryService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class BookSummaryServiceImpl implements IBookSummaryService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${openrouter.api.key}")
    private String apiKey;

    @Value("${openrouter.api.url}")
    private String apiUrl;

    @Override
    public BookSummaryResponseDto getBookSummary(String title, String author) {
        String prompt = "Hãy viết một bản tóm tắt chi tiết (~500 chữ) cho cuốn sách \""
                + title + "\" của tác giả " + author
                + ". Tóm tắt phải đầy đủ cốt truyện, nhân vật chính, chủ đề và thông điệp chính. "
                + "Chỉ trả về văn bản tóm tắt, không thêm bất kỳ giải thích hoặc ghi chú nào khác.";

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

            // lấy text từ response
            String content = root.path("choices").get(0)
                    .path("message")
                    .path("content").asText();

            return new BookSummaryResponseDto(title, author, content);

        } catch (Exception e) {
            throw new RuntimeException("Error calling OpenRouter API: " + e.getMessage(), e);
        }
    }
}
