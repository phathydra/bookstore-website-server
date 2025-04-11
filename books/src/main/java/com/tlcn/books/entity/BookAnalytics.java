package com.tlcn.books.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "book_analytics")
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class BookAnalytics {

    @Id
    private String id;

    private String bookId; // liên kết với sách

    private long viewCount;

    private long addToCartCount;

    private long purchaseCount;
}
