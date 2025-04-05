package com.tlcn.books.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "reviews")
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class Review {

    @Id
    private String reviewId;

    private String bookId;
    private String accountId;

    private int rating;         // số sao: 1 - 5
    private String comment;     // đánh giá nội dung
    private String image;       // link ảnh (có thể đổi sang List<String> nếu cần)
}
