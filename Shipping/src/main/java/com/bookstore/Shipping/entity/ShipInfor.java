package com.bookstore.Shipping.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

// ✅ THÊM CÁC IMPORT NÀY
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexType;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexed;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "shipInfor")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ShipInfor {
    @Id
    private String id;

    private String shipperId;
    private String name;
    private String phone;
    private String address; // Đây là địa chỉ nhà (static)
    private String avatar;

    // Vị trí hiện tại (vẫn giữ để tương thích)
    private Double latitude;
    private Double longitude;
    private LocalDateTime lastUpdated;

    // ✅ TRƯỜNG MỚI: Tối ưu cho truy vấn địa lý (GeoSpatial)
    // Trường này sẽ được index "2dsphere"
    // Nó lưu trữ tọa độ theo định dạng [longitude, latitude]
    @GeoSpatialIndexed(type = GeoSpatialIndexType.GEO_2DSPHERE)
    private GeoJsonPoint currentLocation;

    // Lịch sử lộ trình (giữ tối đa 50 điểm)
    private List<LocationPoint> routeHistory = new ArrayList<>();
}