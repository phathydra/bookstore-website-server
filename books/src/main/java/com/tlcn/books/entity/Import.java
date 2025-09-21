package com.tlcn.books.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Document(collection = "imports")
@NoArgsConstructor
@AllArgsConstructor
public class Import {
    @Id
    private String id;

    // Tham chiếu đến ID sách
    @Field("book_id")
    private String bookId;

    @Field("book_name")
    private String bookName;

    @Field("book_author")
    private String bookAuthor;

    @Field("book_supplier")
    private String bookSupplier;

    private Integer quantity;

    @Field("import_price")
    private Double importPrice;

    @Field("import_date")
    private LocalDateTime importDate;
}
