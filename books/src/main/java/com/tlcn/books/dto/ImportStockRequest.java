// File: com.tlcn.books.dto.ImportStockRequest.java

package com.tlcn.books.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import java.util.List;

@Data
public class ImportStockRequest {
    @Valid
    @NotEmpty(message = "Danh sách sách không được để trống")
    private List<BookDto> books;
}