package com.tlcn.books.service;

import com.tlcn.books.entity.Import;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDateTime;

public interface IImportService {
    Page<Import> findAllImports(LocalDateTime startDate, LocalDateTime endDate, int page, int size);
    ByteArrayInputStream exportImportsByDateRange(LocalDateTime startDate, LocalDateTime endDate) throws IOException;
    void importBooksFromExcel(MultipartFile file) throws IOException;
    double calculateTotalImportPrice(LocalDateTime startDate, LocalDateTime endDate);
}
