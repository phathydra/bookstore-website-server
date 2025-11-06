package com.bookstore.Shipping.service.Impl;

import com.bookstore.Shipping.entity.ShipInfor;
import com.bookstore.Shipping.repository.ShipInforRepository;
import com.bookstore.Shipping.service.IGeocodingService; // ✅ Dịch vụ từ prompt 1
import com.bookstore.Shipping.service.IShippingAssignmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

// ✅ THÊM CÁC IMPORT NÀY
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Metrics;
import org.springframework.data.geo.Point;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ShippingAssignmentServiceImpl implements IShippingAssignmentService {

    // Service Geocoding (từ prompt 1) để lấy tọa độ Hub
    private final IGeocodingService geocodingService;

    // Repository để tìm shipper
    private final ShipInforRepository shipInforRepository;

    @Override
    public List<ShipInfor> findNearbyShippers(String hubAddress, double maxRadiusKm) {

        // 1. Lấy tọa độ của Hub (ĐVVC)
        double[] hubCoords = geocodingService.getCoordinatesFromAddress(hubAddress);

        if (hubCoords == null || (hubCoords[0] == 0 && hubCoords[1] == 0)) {
            System.err.println("Không thể geocode địa chỉ Hub: " + hubAddress);
            return List.of(); // Trả về danh sách rỗng
        }

        // Mapbox trả về [Longitude, Latitude]
        double hubLon = hubCoords[0];
        double hubLat = hubCoords[1];

        // 2. Tạo đối tượng Point và Distance cho truy vấn
        // Point của Spring Data cũng dùng (longitude, latitude)
        Point hubPoint = new Point(hubLon, hubLat);
        Distance maxDistance = new Distance(maxRadiusKm, Metrics.KILOMETERS);

        // 3. Truy vấn MongoDB! (Cực kỳ nhanh)
        // Dùng phương thức đã thêm trong Repository
        // Kết quả đã được tự động sắp xếp từ gần đến xa
        List<ShipInfor> nearbyShippers =
                shipInforRepository.findByCurrentLocationNear(hubPoint, maxDistance);

        return nearbyShippers;
    }
}