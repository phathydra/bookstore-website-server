package com.bookstore.orders.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "monthly_points")
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class MonthlyPoints {
    @Id
    private String id;

    private String accountId;

    private int year;

    private int month;

    private Double point;
}
