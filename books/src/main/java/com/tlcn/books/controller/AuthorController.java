package com.tlcn.books.controller;

import com.tlcn.books.dto.AuthorResponseDto;
import com.tlcn.books.service.IAuthorService;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"}, exposedHeaders = "Content-Disposition")
@RestController
@RequestMapping("/api/author")
public class AuthorController {

    private final IAuthorService authorService;

    public AuthorController(IAuthorService authorService) {
        this.authorService = authorService;
    }

    @GetMapping("/{name}")
    public AuthorResponseDto getAuthor(@PathVariable String name) {
        return authorService.getAuthorInfo(name);
    }
}
