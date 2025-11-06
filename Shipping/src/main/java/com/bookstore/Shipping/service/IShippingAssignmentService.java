package com.bookstore.Shipping.service;

import com.bookstore.Shipping.entity.ShipInfor;
import java.util.List;

public interface IShippingAssignmentService {
    /**
     * Tìm các shipper (đang hoạt động) gần một địa chỉ Hub.
     * @param hubAddress Địa chỉ của Hub (ví dụ: "20 Tăng Nhơn Phú...")
     * @param maxRadiusKm Bán kính tối đa (km)
     * @return Danh sách shipper
     */
    List<ShipInfor> findNearbyShippers(String hubAddress, double maxRadiusKm);
}