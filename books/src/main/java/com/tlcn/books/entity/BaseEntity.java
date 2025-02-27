package com.tlcn.books.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@Document
public abstract class BaseEntity {

    @CreatedDate
    private LocalDateTime createAT;

    @CreatedBy
    private String createBy;

    @LastModifiedDate
    private LocalDateTime updateAT;

    @LastModifiedBy
    private String updateBy;
}
