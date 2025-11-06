package com.bookstore.Shipping.service.Impl;

import com.bookstore.Shipping.entity.LocationPoint;
import com.bookstore.Shipping.entity.ShipInfor;
import com.bookstore.Shipping.repository.ShipInforRepository;
import com.bookstore.Shipping.service.IShipperTrackingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

// ✅ THÊM IMPORT
import org.springframework.data.mongodb.core.geo.GeoJsonPoint;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ShipperTrackingServiceImpl implements IShipperTrackingService {

    private final ShipInforRepository shipInforRepository;

    @Override
    public Optional<ShipInfor> updateLocation(String shipperId, Double latitude, Double longitude) {
        return shipInforRepository.findByShipperId(shipperId)
                .map(shipInfor -> {
                    shipInfor.setLatitude(latitude);
                    shipInfor.setLongitude(longitude);
                    shipInfor.setLastUpdated(LocalDateTime.now());

                    // ✅ CẬP NHẬT TRƯỜNG GEOJSONPOINT
                    // Quan trọng: GeoJsonPoint lưu (longitude, latitude)
                    if (longitude != null && latitude != null) {
                        shipInfor.setCurrentLocation(new GeoJsonPoint(longitude, latitude));
                    } else {
                        shipInfor.setCurrentLocation(null); // Xóa vị trí nếu không hợp lệ
                    }

                    // thêm lịch sử
                    shipInfor.getRouteHistory().add(new LocationPoint(latitude, longitude, LocalDateTime.now()));
                    if (shipInfor.getRouteHistory().size() > 50) {
                        shipInfor.getRouteHistory().remove(0);
                    }

                    return shipInforRepository.save(shipInfor);
                });
    }


    @Override
    public Optional<ShipInfor> getCurrentLocation(String shipperId) {
        return shipInforRepository.findByShipperId(shipperId)
                .stream()
                .findFirst();
    }
}