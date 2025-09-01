package com.tlcn.books.service;

import com.tlcn.books.dto.AuthorResponseDto;

public interface IAuthorService {
    AuthorResponseDto getAuthorInfo(String authorName);
}
