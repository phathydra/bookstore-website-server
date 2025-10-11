package com.bookstore.Shipping.controller;

import com.bookstore.Shipping.entity.ShipInfor;
import com.bookstore.Shipping.service.IShipperTrackingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/shippers")
@RequiredArgsConstructor
public class ShipperTrackingController {

    private final IShipperTrackingService trackingService;

    @PutMapping("/{shipperId}/location")
    public ResponseEntity<ShipInfor> updateLocation(
            @PathVariable String shipperId,
            @RequestParam Double latitude,
            @RequestParam Double longitude) {
        Optional<ShipInfor> updated = trackingService.updateLocation(shipperId, latitude, longitude);
        return updated.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{shipperId}/location")
    public ResponseEntity<ShipInfor> getCurrentLocation(@PathVariable String shipperId) {
        Optional<ShipInfor> location = trackingService.getCurrentLocation(shipperId);
        return location.map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }
}
