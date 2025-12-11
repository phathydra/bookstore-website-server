package com.tlcn.books.controller;

import com.tlcn.books.dto.BannerDto;
import com.tlcn.books.service.IBannerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/banners")
public class BannerController {

    @Autowired
    private IBannerService bannerService;

    // API CHO TRANG CHỦ (Lấy list active để chạy slider)
    @GetMapping("/active")
    public ResponseEntity<List<BannerDto>> getAllActiveBanners() {
        return ResponseEntity.ok(bannerService.getAllActiveBanners());
    }

    // API CHO ADMIN (Lấy list có phân trang để quản lý)
    @GetMapping("")
    public ResponseEntity<Page<BannerDto>> getAllBannersForAdmin(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(bannerService.getAllBanners(page, size));
    }

    // TẠO MỚI BANNER (Dạng Form Data để up ảnh)
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BannerDto> createBanner(
            @RequestPart("banner") String bannerDtoJson,
            @RequestPart(value = "file", required = false) MultipartFile file) {
        try {
            // Convert JSON string từ client thành Object DTO
            ObjectMapper objectMapper = new ObjectMapper();
            BannerDto bannerDto = objectMapper.readValue(bannerDtoJson, BannerDto.class);

            BannerDto newBanner = bannerService.createBanner(bannerDto, file);
            return ResponseEntity.status(HttpStatus.CREATED).body(newBanner);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // UPDATE BANNER
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BannerDto> updateBanner(
            @PathVariable String id,
            @RequestPart("banner") String bannerDtoJson,
            @RequestPart(value = "file", required = false) MultipartFile file) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            BannerDto bannerDto = objectMapper.readValue(bannerDtoJson, BannerDto.class);

            BannerDto updatedBanner = bannerService.updateBanner(id, bannerDto, file);
            return ResponseEntity.ok(updatedBanner);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // XÓA BANNER
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBanner(@PathVariable String id) {
        bannerService.deleteBanner(id);
        return ResponseEntity.noContent().build();
    }
}