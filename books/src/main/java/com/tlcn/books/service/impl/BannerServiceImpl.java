package com.tlcn.books.service.impl;

import com.tlcn.books.dto.BannerDto;
import com.tlcn.books.entity.Banner;
import com.tlcn.books.mapper.BannerMapper;
import com.tlcn.books.repository.BannerRepository;
import com.tlcn.books.service.IBannerService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class BannerServiceImpl implements IBannerService {

    private final BannerRepository bannerRepository;

    // Giả sử bạn có service upload ảnh (nếu lưu local thì tự viết hàm save)
    // private final FileStorageService fileStorageService;

    @Override
    public List<BannerDto> getAllActiveBanners() {
        List<Banner> banners = bannerRepository.findByIsActiveTrue();
        return banners.stream()
                .map(BannerMapper::mapToBannerDto)
                .collect(Collectors.toList());
    }

    @Override
    public Page<BannerDto> getAllBanners(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return bannerRepository.findAllBy(pageable)
                .map(BannerMapper::mapToBannerDto);
    }

    @Override
    public BannerDto createBanner(BannerDto bannerDto, MultipartFile file) {
        Banner banner = new Banner();
        BannerMapper.mapToBanner(bannerDto, banner);

        // --- XỬ LÝ UPLOAD ẢNH Ở ĐÂY ---
        if (file != null && !file.isEmpty()) {
            try {
                // Ví dụ: String imageUrl = fileStorageService.storeFile(file);
                // banner.setImageUrl(imageUrl);

                // Code tạm thời giả lập lưu tên file:
                banner.setImageUrl("http://localhost:8081/images/" + file.getOriginalFilename());
            } catch (Exception e) {
                throw new RuntimeException("Lỗi upload ảnh");
            }
        }
        // ------------------------------

        Banner savedBanner = bannerRepository.save(banner);
        return BannerMapper.mapToBannerDto(savedBanner);
    }

    @Override
    public BannerDto updateBanner(String id, BannerDto bannerDto, MultipartFile file) {
        Banner existingBanner = bannerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Banner not found"));

        // Cập nhật thông tin text
        BannerMapper.mapToBanner(bannerDto, existingBanner);

        // Nếu có up ảnh mới thì thay thế, không thì giữ nguyên ảnh cũ
        if (file != null && !file.isEmpty()) {
            // --- GỌI HÀM UPLOAD ẢNH MỚI ---
            // existingBanner.setImageUrl(newUrl);
        }

        Banner updatedBanner = bannerRepository.save(existingBanner);
        return BannerMapper.mapToBannerDto(updatedBanner);
    }

    @Override
    public void deleteBanner(String id) {
        bannerRepository.deleteById(id);
    }
}