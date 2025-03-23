package com.tlcn.books.entity;

import lombok.*;
import org.springframework.data.annotation.Id;

import java.util.List;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class Condition {

    private Double minOrderValue;
    private List<String> applicableCategories;
    private List<String> applicableUsers;
    private Integer usageLimit;
}
