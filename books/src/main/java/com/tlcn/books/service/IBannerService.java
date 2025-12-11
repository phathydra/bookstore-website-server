package com.tlcn.books.service;

import com.tlcn.books.dto.BannerDto;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IBannerService {
    // Cho trang chủ (Frontend)
    List<BannerDto> getAllActiveBanners();

    // Cho Admin quản lý
    Page<BannerDto> getAllBanners(int page, int size);

    // Tạo mới banner (kèm upload ảnh)
    BannerDto createBanner(BannerDto bannerDto, MultipartFile file);

    // Update banner
    BannerDto updateBanner(String id, BannerDto bannerDto, MultipartFile file);

    // Xóa banner
    void deleteBanner(String id);
}