package com.tlcn.books.mapper;

import com.tlcn.books.dto.BannerDto;
import com.tlcn.books.entity.Banner;

public class BannerMapper {
    public static BannerDto mapToBannerDto(Banner banner) {
        BannerDto dto = new BannerDto();
        dto.setId(banner.getId());
        dto.setTitle(banner.getTitle());
        dto.setImageUrl(banner.getImageUrl());
        dto.setLinkUrl(banner.getLinkUrl());
        dto.setPosition(banner.getPosition());
        dto.setActive(banner.isActive());
        return dto;
    }

    public static Banner mapToBanner(BannerDto dto, Banner banner) {
        banner.setTitle(dto.getTitle());
        // ImageUrl thường được set riêng sau khi upload file
        if (dto.getImageUrl() != null) {
            banner.setImageUrl(dto.getImageUrl());
        }
        banner.setLinkUrl(dto.getLinkUrl());
        banner.setPosition(dto.getPosition());
        banner.setActive(dto.isActive());
        return banner;
    }
}