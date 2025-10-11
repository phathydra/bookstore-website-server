package com.bookstore.Shipping.controller;

import com.bookstore.Shipping.dto.ShippingDto;
import com.bookstore.Shipping.entity.DeliveryUnit;
import com.bookstore.Shipping.entity.ShipInfor;
import com.bookstore.Shipping.entity.Shipping;
import com.bookstore.Shipping.service.IShippingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"}, exposedHeaders = "Content-Disposition")
@RestController
@RequestMapping("/api/shipping")
public class ShippingController {

    @Autowired
    private IShippingService shippingService;

    @PostMapping("/create")
    // Đổi tên phương thức để phản ánh việc tạo account chung hơn
    public ResponseEntity<Shipping> createAccount(@RequestBody ShippingDto dto) {
        Shipping account = shippingService.createAccount(dto);
        return ResponseEntity.ok(account);
    }

    @GetMapping("/shippers")
    public ResponseEntity<List<Shipping>> getAllShippersOnly() {
        return ResponseEntity.ok(shippingService.getAllShippersOnly());
    }

    @GetMapping("/delivery-units")
    public ResponseEntity<List<Shipping>> getAllDeliveryUnits() {
        return ResponseEntity.ok(shippingService.getAllDeliveryUnits());
    }

    @GetMapping("/delivery-units/{deliveryUnitId}/address")
    public ResponseEntity<String> getDeliveryUnitAddress(@PathVariable String deliveryUnitId) {
        Optional<String> addressOptional = shippingService.getAddressByDeliveryUnitId(deliveryUnitId);

        if (addressOptional.isPresent()) {
            return ResponseEntity.ok(addressOptional.get());
        } else {
            // Trả về 404 nếu không tìm thấy DeliveryUnit
            return new ResponseEntity<>("Delivery Unit not found or address not set", HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("/delivery-units/{deliveryUnitId}/update")
    public ResponseEntity<?> updateDeliveryUnit(
            @PathVariable String deliveryUnitId,
            @RequestBody DeliveryUnit updatedUnit) {

        Optional<DeliveryUnit> result = shippingService.updateDeliveryUnit(deliveryUnitId, updatedUnit);

        if (result.isPresent()) {
            return ResponseEntity.ok(result.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Delivery Unit not found with ID: " + deliveryUnitId);
        }
    }

    @GetMapping("/delivery-units/{deliveryUnitId}")
    public ResponseEntity<?> getDeliveryUnitById(@PathVariable String deliveryUnitId) {
        Optional<DeliveryUnit> unitOptional = shippingService.getDeliveryUnitById(deliveryUnitId);
        if (unitOptional.isPresent()) {
            return ResponseEntity.ok(unitOptional.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Delivery Unit not found with ID: " + deliveryUnitId);
        }
    }
    @GetMapping("shipInfor/{shipperId}")
    public ResponseEntity<?> getShipInfor(@PathVariable String shipperId) {
        return shippingService.getShipInforByShipperId(shipperId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ✅ PUT: Cập nhật thông tin shipper theo shipperId
    @PutMapping("shipInfor/{shipperId}")
    public ResponseEntity<?> updateShipInfor(
            @PathVariable String shipperId,
            @RequestBody ShipInfor updatedInfo
    ) {
        return shippingService.updateShipInfor(shipperId, updatedInfo)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}