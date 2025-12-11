package com.tlcn.books.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "banners")
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class Banner {

    @Id
    private String id;

    private String title;       // Tên banner (để quản lý cho dễ)
    private String imageUrl;    // Link ảnh (đã upload)
    private String linkUrl;     // Link điều hướng khi click vào banner

    // Vị trí: 'MAIN_SLIDER', 'RIGHT_TOP', 'RIGHT_BOTTOM'
    private String position;

    private boolean isActive;   // Trạng thái hiển thị
}