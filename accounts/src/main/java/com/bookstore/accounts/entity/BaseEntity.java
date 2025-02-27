package com.bookstore.accounts.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.mongodb.core.mapping.Field;

@Getter @Setter @ToString
public class BaseEntity {

    @Field("created_by")
    private String createdBy;

    @Field("updated_by")
    private String updatedBy;
}
