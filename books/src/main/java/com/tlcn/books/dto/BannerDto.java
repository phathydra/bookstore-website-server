package com.tlcn.books.dto;

import lombok.Data;

@Data
public class BannerDto {
    private String id;
    private String title;
    private String imageUrl;
    private String linkUrl;
    private String position;
    private boolean isActive;
}