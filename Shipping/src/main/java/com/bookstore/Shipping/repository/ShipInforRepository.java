package com.bookstore.Shipping.repository;

import com.bookstore.Shipping.entity.ShipInfor;
// ✅ THÊM CÁC IMPORT NÀY
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Point;

import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;
import java.util.Optional;

public interface ShipInforRepository extends MongoRepository<ShipInfor, String> {

    // (Phương thức bạn đã có)
    Optional<ShipInfor> findByShipperId(String shipperId);

    // ✅ PHƯƠNG THỨC MỚI (TỰ ĐỘNG CỦA SPRING DATA)
    /**
     * Tìm các shipper có trường 'currentLocation' gần một 'point'
     * trong phạm vi 'distance'.
     * Kết quả trả về đã được tự động sắp xếp từ gần đến xa.
     *
     * @param point Tọa độ của Hub (Lon, Lat)
     * @param distance Bán kính tối đa
     * @return Danh sách shipper phù hợp
     */
    List<ShipInfor> findByCurrentLocationNear(Point point, Distance distance);
}