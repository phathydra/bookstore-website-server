package com.tlcn.books.controller;

import com.tlcn.books.dto.ImportPreviewResponse;
import com.tlcn.books.service.IImportService;
import com.tlcn.books.entity.Import;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.core.io.Resource;
import org.springframework.core.io.InputStreamResource;

@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"}, exposedHeaders = "Content-Disposition")
@RestController
@RequestMapping("/api/imports")
public class ImportController {

    private final IImportService iImportService;

    @Autowired
    public ImportController(IImportService iImportService) {
        this.iImportService = iImportService;
    }

    @GetMapping
    public ResponseEntity<Page<Import>> getAllImports(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<Import> imports = iImportService.findAllImports(startDate, endDate, page, size);
        return new ResponseEntity<>(imports, HttpStatus.OK);
    }

    @GetMapping("/export")
    public ResponseEntity<Resource> exportImports(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        try {
            ByteArrayInputStream in = iImportService.exportImportsByDateRange(startDate, endDate);

            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Disposition", "attachment; filename=imports.xlsx");

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType.parseMediaType(
                            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(new InputStreamResource(in));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/import")
    public ResponseEntity<String> importBooksFromExcel(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Vui lòng chọn một file để tải lên.");
        }
        if (!"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet".equals(file.getContentType())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("File không đúng định dạng Excel (.xlsx).");
        }
        try {
            iImportService.importBooksFromExcel(file);
            return ResponseEntity.ok("Nhập sách từ file Excel thành công!");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Lỗi khi đọc file: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Đã xảy ra lỗi hệ thống: " + e.getMessage());
        }
    }

    @GetMapping("/total-price")
    public ResponseEntity<Map<String, Double>> getTotalImportPrice(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        try {
            double total = iImportService.calculateTotalImportPrice(startDate, endDate);
            Map<String, Double> response = new HashMap<>();
            response.put("totalPrice", total);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/preview")
    public ResponseEntity<?> previewExcel(@RequestParam("file") MultipartFile file) {
        try {
            ImportPreviewResponse preview = iImportService.previewExcelContent(file);
            return ResponseEntity.ok(preview);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Lỗi đọc file: " + e.getMessage());
        }
    }

    @PostMapping("/confirm-import")
    public ResponseEntity<?> confirmImport(@RequestBody ImportPreviewResponse confirmedData) {
        try {
            iImportService.saveImportData(confirmedData);
            return ResponseEntity.ok("Nhập kho thành công!");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi hệ thống: " + e.getMessage());
        }
    }
}