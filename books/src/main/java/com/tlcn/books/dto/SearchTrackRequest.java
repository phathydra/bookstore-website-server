package com.tlcn.books.dto;

import lombok.Data;

@Data
public class SearchTrackRequest {
    private String searchTerm;
    private String accountId;
}