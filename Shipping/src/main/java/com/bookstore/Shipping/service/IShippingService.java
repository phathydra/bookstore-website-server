package com.bookstore.Shipping.service;

import com.bookstore.Shipping.dto.ShippingDto;
import com.bookstore.Shipping.entity.DeliveryUnit;
import com.bookstore.Shipping.entity.ShipInfor;
import com.bookstore.Shipping.entity.Shipping;

import java.util.List;
import java.util.Optional;

public interface IShippingService {
    // Đổi tên phương thức
    Shipping createAccount(ShippingDto dto);

    List<Shipping> getAllShippersOnly();
    List<Shipping> getAllDeliveryUnits();
    Optional<String> getAddressByDeliveryUnitId(String deliveryUnitId);
    Optional<String> getCustomerAddressByOrderId(String orderId);
    Optional<DeliveryUnit> updateDeliveryUnit(String deliveryUnitId, DeliveryUnit updatedUnit);
    Optional<DeliveryUnit> getDeliveryUnitById(String deliveryUnitId);
    Optional<ShipInfor> getShipInforByShipperId(String shipperId);
    Optional<ShipInfor> updateShipInfor(String shipperId, ShipInfor updatedInfo);
}