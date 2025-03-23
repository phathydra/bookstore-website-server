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
    String id;

    int percentage;

    Date startDate;
    Date endDate;
}
