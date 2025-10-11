package com.bookstore.Shipping.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

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

    private String shipperId;  // trỏ đến accountId trong Shipping
    private String name;
    private String phone;
    private String address;
    private String avatar;

    // Vị trí hiện tại
    private Double latitude;
    private Double longitude;
    private LocalDateTime lastUpdated;

    // Lịch sử lộ trình (giữ tối đa 50 điểm)
    private List<LocationPoint> routeHistory = new ArrayList<>();
}
