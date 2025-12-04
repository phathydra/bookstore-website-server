package com.tlcn.books.dto;

import lombok.Data;

@Data
public class FilterTrackRequest {
    private String accountId;
    private String filterData; // Sẽ nhận một chuỗi JSON từ frontend
}