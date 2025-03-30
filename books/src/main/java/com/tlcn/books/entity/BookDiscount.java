package com.tlcn.books.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "book_discounts")
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class BookDiscount {
    @Id
    private String id;

    private String bookId;
    private String discountId;
}
