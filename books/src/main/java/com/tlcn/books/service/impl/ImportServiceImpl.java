package com.tlcn.books.service.impl;

import com.tlcn.books.entity.Book;
import com.tlcn.books.entity.Import;
import com.tlcn.books.repository.BookRepository;
import com.tlcn.books.repository.ImportRepository;
import com.tlcn.books.service.IImportService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ImportServiceImpl implements IImportService {

    private final ImportRepository importRepository;
    private final BookRepository bookRepository; // Thêm BookRepository để thao tác với sách

    @Autowired
    public ImportServiceImpl(ImportRepository importRepository, BookRepository bookRepository) {
        this.importRepository = importRepository;
        this.bookRepository = bookRepository;
    }

    @Override
    public Page<Import> findAllImports(LocalDateTime startDate, LocalDateTime endDate, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("importDate").descending());

        if (startDate != null && endDate != null) {
            return importRepository.findByImportDateBetween(startDate, endDate, pageRequest);
        } else {
            return importRepository.findAll(pageRequest);
        }
    }

    @Override
    public ByteArrayInputStream exportImportsByDateRange(LocalDateTime startDate, LocalDateTime endDate) throws IOException {
        List<Import> imports;

        if (startDate != null && endDate != null) {
            imports = importRepository.findByImportDateBetween(startDate, endDate);
        } else {
            imports = importRepository.findAll();
        }

        return exportImportsToExcel(imports);
    }

    private ByteArrayInputStream exportImportsToExcel(List<Import> imports) throws IOException {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Imports");
            String[] headers = {
                    "Import ID", "Book ID", "Book Name", "Author", "Supplier",
                    "Quantity", "Import Price", "Import Date"
            };

            // Tạo style cho header
            Font font = workbook.createFont();
            font.setBold(true);
            CellStyle headerCellStyle = workbook.createCellStyle();
            headerCellStyle.setFont(font);

            // Header row
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerCellStyle);
            }

            int rowIdx = 1;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

            for (Import imp : imports) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(imp.getId());
                row.createCell(1).setCellValue(imp.getBookId());
                row.createCell(2).setCellValue(imp.getBookName());
                row.createCell(3).setCellValue(imp.getBookAuthor());
                row.createCell(4).setCellValue(imp.getBookSupplier());
                row.createCell(5).setCellValue(imp.getQuantity() != null ? imp.getQuantity() : 0);
                row.createCell(6).setCellValue(imp.getImportPrice() != null ? imp.getImportPrice() : 0.0);
                row.createCell(7).setCellValue(
                        imp.getImportDate() != null ? imp.getImportDate().format(formatter) : ""
                );
            }

            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        }
    }

    @Override
    @Transactional
    public void importBooksFromExcel(MultipartFile file) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                String bookName = getCellValueAsString(row.getCell(0));   // Cột A
                String bookAuthor = getCellValueAsString(row.getCell(1)); // Cột B
                String bookSupplier = getCellValueAsString(row.getCell(2)); // Cột C
                int quantity = getCellValueAsInt(row.getCell(3));        // Cột D
                double importPrice = getCellValueAsDouble(row.getCell(4)); // Cột E

                if (bookName.isEmpty() || bookAuthor.isEmpty() || quantity <= 0) continue;

                Optional<Book> existingBookOpt = bookRepository.findByBookNameIgnoreCaseAndBookAuthorIgnoreCase(bookName, bookAuthor);
                Book book;
                String bookId;

                if (existingBookOpt.isPresent()) {
                    // Nếu sách đã tồn tại, cập nhật số lượng
                    book = existingBookOpt.get();
                    book.setBookStockQuantity(book.getBookStockQuantity() + quantity);
                    bookRepository.save(book);
                    bookId = book.getBookId();
                } else {
                    // Nếu sách chưa tồn tại, tạo mới
                    book = new Book();
                    book.setBookId(UUID.randomUUID().toString());
                    book.setBookName(bookName);
                    book.setBookAuthor(bookAuthor);
                    book.setBookSupplier(bookSupplier);
                    book.setBookStockQuantity(quantity);
                    bookRepository.save(book);
                    bookId = book.getBookId();
                }

                // Tạo bản ghi nhập kho
                Import newImport = new Import();
                newImport.setBookId(bookId);
                newImport.setBookName(bookName);
                newImport.setBookAuthor(bookAuthor);
                newImport.setBookSupplier(bookSupplier);
                newImport.setQuantity(quantity);
                newImport.setImportPrice(importPrice);
                newImport.setImportDate(LocalDateTime.now());
                importRepository.save(newImport);
            }
        }
    }

    // Helper methods to safely get cell values
    private String getCellValueAsString(Cell cell) {
        if (cell == null) return "";
        cell.setCellType(CellType.STRING);
        return cell.getStringCellValue();
    }

    private int getCellValueAsInt(Cell cell) {
        if (cell == null) return 0;
        if (cell.getCellType() == CellType.NUMERIC) {
            return (int) cell.getNumericCellValue();
        }
        return 0;
    }

    private double getCellValueAsDouble(Cell cell) {
        if (cell == null) return 0.0;
        if (cell.getCellType() == CellType.NUMERIC) {
            return cell.getNumericCellValue();
        }
        return 0.0;
    }

    @Override
    public double calculateTotalImportPrice(LocalDateTime startDate, LocalDateTime endDate) {
        List<Import> imports;
        if (startDate != null && endDate != null) {
            imports = importRepository.findByImportDateBetween(startDate, endDate);
        } else {
            imports = importRepository.findAll();
        }

        return imports.stream()
                .mapToDouble(imp -> imp.getQuantity() * imp.getImportPrice())
                .sum();
    }
}