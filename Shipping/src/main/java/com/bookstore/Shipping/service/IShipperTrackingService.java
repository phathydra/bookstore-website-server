package com.bookstore.Shipping.service;

import com.bookstore.Shipping.entity.ShipInfor;
import java.util.Optional;

public interface IShipperTrackingService {
    Optional<ShipInfor> updateLocation(String shipperId, Double latitude, Double longitude);
    Optional<ShipInfor> getCurrentLocation(String shipperId);
}
