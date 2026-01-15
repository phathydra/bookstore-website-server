package com.tlcn.books.service.impl;

import com.tlcn.books.dto.ImportPreviewResponse;
import com.tlcn.books.dto.ImportRequestDTO;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ImportServiceImpl implements IImportService {

    private final ImportRepository importRepository;
    private final BookRepository bookRepository;

    @Autowired
    public ImportServiceImpl(ImportRepository importRepository, BookRepository bookRepository) {
        this.importRepository = importRepository;
        this.bookRepository = bookRepository;
    }

    // =================================================================================
    // 1. SMART MATCHING ALGORITHMS
    // =================================================================================

    private String normalizeString(String input) {
        if (input == null) return "";
        return input.trim().replaceAll("\\s+", " ");
    }

    private double calculateSimilarity(String s1, String s2) {
        String longer = s1, shorter = s2;
        if (s1.length() < s2.length()) {
            longer = s2; shorter = s1;
        }
        int longerLength = longer.length();
        if (longerLength == 0) return 1.0;
        int editDistance = getLevenshteinDistance(longer, shorter);
        return (longerLength - editDistance) / (double) longerLength;
    }

    private int getLevenshteinDistance(String x, String y) {
        int[][] dp = new int[x.length() + 1][y.length() + 1];
        for (int i = 0; i <= x.length(); i++) dp[i][0] = i;
        for (int j = 0; j <= y.length(); j++) dp[0][j] = j;
        for (int i = 1; i <= x.length(); i++) {
            for (int j = 1; j <= y.length(); j++) {
                int cost = (x.charAt(i - 1) == y.charAt(j - 1)) ? 0 : 1;
                dp[i][j] = Math.min(Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1), dp[i - 1][j - 1] + cost);
            }
        }
        return dp[x.length()][y.length()];
    }

    // =================================================================================
    // 2. LOGIC PREVIEW & IMPORT (NÂNG CẤP)
    // =================================================================================

    @Override
    public ImportPreviewResponse previewExcelContent(MultipartFile file) throws IOException {
        ImportPreviewResponse response = new ImportPreviewResponse();
        List<ImportRequestDTO> newBooks = new ArrayList<>();
        List<ImportRequestDTO> existingBooks = new ArrayList<>();

        List<Book> allBooksInDb = bookRepository.findAll();

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                String rawName = getCellValueAsString(row.getCell(0));
                String rawAuthor = getCellValueAsString(row.getCell(1));
                String supplier = getCellValueAsString(row.getCell(2));
                int quantity = getCellValueAsInt(row.getCell(3));
                double price = getCellValueAsDouble(row.getCell(4));

                if (rawName.isEmpty() || rawAuthor.isEmpty() || quantity <= 0) continue;

                String cleanName = normalizeString(rawName);
                String cleanAuthor = normalizeString(rawAuthor);

                ImportRequestDTO dto = new ImportRequestDTO();
                dto.setBookName(cleanName);
                dto.setBookAuthor(cleanAuthor);
                dto.setBookSupplier(supplier);
                dto.setBookStockQuantity(quantity);
                dto.setImportPrice(price);

                // BƯỚC 1: Tìm chính xác 100%
                Optional<Book> exactMatch = allBooksInDb.stream()
                        .filter(b -> b.getBookName().equalsIgnoreCase(cleanName) && b.getBookAuthor().equalsIgnoreCase(cleanAuthor))
                        .findFirst();

                if (exactMatch.isPresent()) {
                    dto.setId(exactMatch.get().getBookId());
                    existingBooks.add(dto);
                } else {
                    // BƯỚC 2: Tìm danh sách các sách có khả năng giống (Top Suggestions)
                    List<ImportRequestDTO.SuggestionDTO> matches = new ArrayList<>();

                    for (Book dbBook : allBooksInDb) {
                        // Chỉ so sánh nếu tác giả trùng (để tăng tốc và chính xác)
                        if (dbBook.getBookAuthor().equalsIgnoreCase(cleanAuthor)) {
                            double score = calculateSimilarity(cleanName.toLowerCase(), dbBook.getBookName().toLowerCase());

                            // Hạ ngưỡng xuống 0.4 (40%) để bắt được Tập 1 vs Tập 2
                            if (score >= 0.40) {
                                matches.add(new ImportRequestDTO.SuggestionDTO(
                                        dbBook.getBookId(),
                                        dbBook.getBookName(),
                                        (int) Math.round(score * 100)
                                ));
                            }
                        }
                    }

                    // Sắp xếp theo độ giống giảm dần
                    matches.sort((a, b) -> b.getSimilarity() - a.getSimilarity());

                    // Lấy Top 5 cuốn
                    List<ImportRequestDTO.SuggestionDTO> topMatches = matches.stream().limit(5).collect(Collectors.toList());

                    dto.setId(UUID.randomUUID().toString()); // ID tạm
                    dto.setSuggestions(topMatches);

                    if (!topMatches.isEmpty()) {
                        // Có gợi ý -> Đánh dấu cảnh báo
                        ImportRequestDTO.SuggestionDTO best = topMatches.get(0);
                        // Nếu độ giống > 50% thì mới set default, không thì chỉ hiện list
                        if (best.getSimilarity() > 50) {
                            dto.setWarning("Tìm thấy " + topMatches.size() + " sách tương tự. Bấm kính lúp để chọn.");
                            dto.setSuggestedBookId(best.getId());
                            dto.setSuggestedBookName(best.getName());
                        }
                    } else {
                        dto.setMainCategory("Chưa phân loại");
                    }
                    newBooks.add(dto);
                }
            }
        }

        response.setNewBooks(newBooks);
        response.setExistingBooks(existingBooks);
        return response;
    }

    @Override
    @Transactional
    public void saveImportData(ImportPreviewResponse confirmedData) {
        // 1. SÁCH MỚI
        if (confirmedData.getNewBooks() != null) {
            for (ImportRequestDTO dto : confirmedData.getNewBooks()) {
                Book book = new Book();
                book.setBookName(dto.getBookName());
                book.setBookAuthor(dto.getBookAuthor());
                book.setBookSupplier(dto.getBookSupplier());
                book.setBookStockQuantity(dto.getBookStockQuantity());
                book.setBookPrice(dto.getImportPrice() > 0 ? dto.getImportPrice() : 0.0);
                book.setBookImages(List.of("https://res.cloudinary.com/dfsxqmwkz/image/upload/v1761570462/li3gbhoqxbxouyidcbpm.jpg"));
                book.setMainCategory("Chưa phân loại");
                book.setBookCategory("Chưa phân loại");
                book.setBookYearOfProduction(0);
                book.setBookPublisher("Không rõ");
                book.setBookLanguage("Không rõ");
                book.setBookDescription("Mô tả đang cập nhật");

                bookRepository.save(book);
                saveImportRecord(book.getBookId(), dto);
            }
        }

        // 2. SÁCH CŨ
        if (confirmedData.getExistingBooks() != null) {
            for (ImportRequestDTO dto : confirmedData.getExistingBooks()) {
                Optional<Book> bookOpt = bookRepository.findById(dto.getId());
                if (bookOpt.isPresent()) {
                    Book book = bookOpt.get();
                    // Chỉ cộng dồn số lượng
                    book.setBookStockQuantity(book.getBookStockQuantity() + dto.getBookStockQuantity());
                    // Tuyệt đối KHÔNG cập nhật tên/tác giả để tránh sai lệch dữ liệu gốc
                    bookRepository.save(book);
                    saveImportRecord(book.getBookId(), dto);
                }
            }
        }
    }

    private void saveImportRecord(String bookId, ImportRequestDTO dto) {
        Import imp = new Import();
        imp.setBookId(bookId);
        imp.setBookName(dto.getBookName());
        imp.setBookAuthor(dto.getBookAuthor());
        imp.setBookSupplier(dto.getBookSupplier());
        imp.setQuantity(dto.getBookStockQuantity());
        imp.setImportPrice(dto.getImportPrice());
        imp.setImportDate(LocalDateTime.now());
        importRepository.save(imp);
    }

    // --- CÁC HÀM CŨ GIỮ NGUYÊN (FindAll, Export, Helper...) ---
    @Override
    public Page<Import> findAllImports(LocalDateTime startDate, LocalDateTime endDate, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("importDate").descending());
        if (startDate != null && endDate != null) {
            return importRepository.findByImportDateBetween(startDate, endDate, pageRequest);
        }
        return importRepository.findAll(pageRequest);
    }

    @Override
    public double calculateTotalImportPrice(LocalDateTime startDate, LocalDateTime endDate) {
        List<Import> imports;
        if (startDate != null && endDate != null) {
            imports = importRepository.findByImportDateBetween(startDate, endDate);
        } else {
            imports = importRepository.findAll();
        }
        return imports.stream().mapToDouble(imp -> imp.getQuantity() * imp.getImportPrice()).sum();
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
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Lịch sử nhập kho");
            String[] headers = {"Import ID", "Book ID", "Book Name", "Author", "Supplier", "Quantity", "Import Price", "Import Date"};
            Font font = workbook.createFont(); font.setBold(true);
            CellStyle headerCellStyle = workbook.createCellStyle(); headerCellStyle.setFont(font);
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i); cell.setCellValue(headers[i]); cell.setCellStyle(headerCellStyle);
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
                row.createCell(7).setCellValue(imp.getImportDate() != null ? imp.getImportDate().format(formatter) : "");
            }
            for (int i = 0; i < headers.length; i++) sheet.autoSizeColumn(i);
            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        }
    }

    @Override
    @Transactional
    public void importBooksFromExcel(MultipartFile file) throws IOException {
        ImportPreviewResponse preview = previewExcelContent(file);
        saveImportData(preview);
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) return "";
        try { cell.setCellType(CellType.STRING); return cell.getStringCellValue(); } catch (Exception e) { return ""; }
    }
    private int getCellValueAsInt(Cell cell) {
        if (cell == null) return 0;
        if (cell.getCellType() == CellType.NUMERIC) return (int) cell.getNumericCellValue();
        else if (cell.getCellType() == CellType.STRING) { try { return Integer.parseInt(cell.getStringCellValue().trim()); } catch (Exception e) { return 0; } }
        return 0;
    }
    private double getCellValueAsDouble(Cell cell) {
        if (cell == null) return 0.0;
        if (cell.getCellType() == CellType.NUMERIC) return cell.getNumericCellValue();
        else if (cell.getCellType() == CellType.STRING) { try { return Double.parseDouble(cell.getStringCellValue().trim()); } catch (Exception e) { return 0.0; } }
        return 0.0;
    }
}