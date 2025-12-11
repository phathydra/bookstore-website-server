package com.tlcn.books.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document(collection = "discounts")
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class Discount {

    @Id
    private String id;

    private int percentage;

    private Date startDate;
    private Date endDate;

    private String type;
}
