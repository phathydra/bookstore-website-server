package com.tlcn.books.repository;

import com.tlcn.books.entity.Banner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BannerRepository extends MongoRepository<Banner, String> {
    // Lấy danh sách banner đang active để hiện ra trang chủ
    List<Banner> findByIsActiveTrue();

    // Lấy tất cả để quản lý trong admin (có phân trang)
    Page<Banner> findAllBy(Pageable pageable);
}