package com.bookstore.orders.service.impl;

import com.bookstore.orders.dto.BookDataForCartDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class BookDataClientService {

    @Autowired
    private RestTemplate restTemplate;

    // Lấy URL của books service từ application.properties
    @Value("${services.books.url}")
    private String booksServiceUrl; // (ví dụ: http://localhost:8080)

    /**
     * Gọi sang 'books' service để lấy thông tin sách mới nhất.
     * GIẢ ĐỊNH: Bạn đã cập nhật API 'POST /api/book/details-by-ids'
     * trong 'books' service để trả về List<BookDataForCartDto>
     */
    public Map<String, BookDataForCartDto> getUpToDateBookData(List<String> bookIds) {
        if (bookIds == null || bookIds.isEmpty()) {
            return Map.of();
        }

        String url = booksServiceUrl + "/api/book/details-by-ids";

        try {
            // Gọi API và nhận mảng BookDataForCartDto
            ResponseEntity<BookDataForCartDto[]> response =
                    restTemplate.postForEntity(url, bookIds, BookDataForCartDto[].class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                // Chuyển mảng thành Map<BookId, BookData> để tra cứu dễ dàng
                return List.of(response.getBody()).stream()
                        .collect(Collectors.toMap(BookDataForCartDto::getBookId, Function.identity()));
            }
        } catch (Exception e) {
            // Xử lý lỗi khi gọi service (ví dụ: log lại)
            System.err.println("Lỗi khi gọi books service: " + e.getMessage());
        }

        return Map.of(); // Trả về map rỗng nếu lỗi
    }
}