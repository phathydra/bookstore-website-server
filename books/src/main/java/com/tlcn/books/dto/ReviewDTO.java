package com.tlcn.books.dto;

import lombok.*;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class ReviewDTO {

    private String reviewId;
    private String bookId;
    private String accountId;

    private int rating;
    private String comment;
    private String image;
}
