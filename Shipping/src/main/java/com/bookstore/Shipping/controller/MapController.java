package com.bookstore.Shipping.controller;

import com.bookstore.Shipping.dto.Coordinate;
import com.bookstore.Shipping.dto.RouteResponse;
import com.bookstore.Shipping.service.IDirectionsService;
import com.bookstore.Shipping.service.IGeocodingService;
import com.bookstore.Shipping.service.IShippingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001", "http://localhost:3002"}, exposedHeaders = "Content-Disposition")
@RestController
@RequestMapping("/api/map")
public class MapController {

    private final IGeocodingService geocodingService;
    private final IDirectionsService directionsService;
    private final IShippingService shippingService; // Đã khai báo

    // ✅ SỬA: Cập nhật Constructor để Inject IShippingService
    public MapController(IGeocodingService geocodingService, IDirectionsService directionsService, IShippingService shippingService) {
        this.geocodingService = geocodingService;
        this.directionsService = directionsService;
        this.shippingService = shippingService; // ✅ Khởi tạo biến
    }

    @GetMapping("/route")
    public ResponseEntity<RouteResponse> getRoute(
            @RequestParam String originAddress,
            @RequestParam String destinationAddress) {

        // GeocodingServiceImpl trả về [lon, lat]
        double[] originCoords = geocodingService.getCoordinatesFromAddress(originAddress);
        double[] destCoords = geocodingService.getCoordinatesFromAddress(destinationAddress);

        double lonOrigin = originCoords[0]; // Kinh độ
        double latOrigin = originCoords[1]; // Vĩ độ
        double lonDest   = destCoords[0];   // Kinh độ
        double latDest   = destCoords[1];   // Vĩ độ

        // Kiểm tra lỗi
        if ((lonOrigin == 0 && latOrigin == 0) || (lonDest == 0 && latDest == 0)) {
            System.err.println("Geocoding failed for one or both addresses.");
            return ResponseEntity.badRequest().body(new RouteResponse("Geocoding failed or address not found."));
        }

        // Log để debug
        System.out.printf("Origin: lon=%f, lat=%f%n", lonOrigin, latOrigin);
        System.out.printf("Destination: lon=%f, lat=%f%n", lonDest, latDest);

        // Mapbox Directions API yêu cầu: (longitude, latitude)
        Coordinate origin = new Coordinate(lonOrigin, latOrigin);
        Coordinate destination = new Coordinate(lonDest, latDest);

        RouteResponse directions = directionsService.getRoute(origin, destination);

        return ResponseEntity.ok(directions);
    }

    @GetMapping("/route/to-delivery-unit")
    public ResponseEntity<RouteResponse> getRouteToDeliveryUnit(
            @RequestParam double currentLon,
            @RequestParam double currentLat,
            @RequestParam String deliveryUnitId) {

        // 1. Lấy địa chỉ của Đơn vị Vận chuyển
        Optional<String> addressOptional = shippingService.getAddressByDeliveryUnitId(deliveryUnitId);

        if (addressOptional.isEmpty() || addressOptional.get().equals("null") || addressOptional.get().isBlank()) {
            return new ResponseEntity<>(
                    new RouteResponse("Delivery Unit address not found or not set."),
                    HttpStatus.NOT_FOUND);
        }

        String destinationAddress = addressOptional.get();

        // 2. Geocode địa chỉ -> tọa độ (lon, lat)
        double[] destCoords = geocodingService.getCoordinatesFromAddress(destinationAddress);
        if (destCoords == null || destCoords.length < 2) {
            return ResponseEntity.badRequest().body(new RouteResponse("Failed to geocode Delivery Unit address."));
        }

        double lonDest = destCoords[0]; // Kinh độ
        double latDest = destCoords[1]; // Vĩ độ

        if (lonDest == 0 && latDest == 0) {
            return ResponseEntity.badRequest().body(new RouteResponse("Invalid geocoded coordinates for Delivery Unit."));
        }

        // 3. Tạo tọa độ xuất phát và đích
        Coordinate origin = new Coordinate(currentLon, currentLat);
        Coordinate destination = new Coordinate(lonDest, latDest);

        System.out.printf("🚚 Route request: Origin(lon=%.6f, lat=%.6f) -> Destination(lon=%.6f, lat=%.6f)%n",
                currentLon, currentLat, lonDest, latDest);

        // 4. Gọi Directions Service
        RouteResponse directions = directionsService.getRoute(origin, destination);

        if (directions == null || directions.getRoutes() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new RouteResponse("Failed to fetch route from Mapbox."));
        }

        return ResponseEntity.ok(directions);
    }

    @GetMapping("/route/to-customer-address")
    public ResponseEntity<RouteResponse> getRouteToCustomerAddress(
            @RequestParam double currentLon,
            @RequestParam double currentLat,
            @RequestParam String orderId) {

        // 1. GỌI SHIPPING SERVICE ĐỂ LẤY ĐỊA CHỈ KHÁCH HÀNG
        Optional<String> addressOptional = shippingService.getCustomerAddressByOrderId(orderId);

        if (addressOptional.isEmpty() || addressOptional.get().isBlank()) {
            return new ResponseEntity<>(
                    new RouteResponse("Customer address for Order ID " + orderId + " not found or empty."),
                    HttpStatus.NOT_FOUND);
        }

        String destinationAddress = addressOptional.get();

        // 2. Geocode địa chỉ -> tọa độ (lon, lat)
        double[] destCoords = geocodingService.getCoordinatesFromAddress(destinationAddress);
        if (destCoords == null || destCoords.length < 2 || (destCoords[0] == 0 && destCoords[1] == 0)) {
            return ResponseEntity.badRequest().body(new RouteResponse("Failed to geocode Customer address: " + destinationAddress));
        }

        double lonDest = destCoords[0];
        double latDest = destCoords[1];

        // 3. Tạo tọa độ xuất phát và đích
        Coordinate origin = new Coordinate(currentLon, currentLat);
        Coordinate destination = new Coordinate(lonDest, latDest);

        System.out.printf("🛵 Route request: Shipper(lon=%.6f, lat=%.6f) -> Customer(lon=%.6f, lat=%.6f) | Address: %s%n",
                currentLon, currentLat, lonDest, latDest, destinationAddress);

        // 4. Gọi Directions Service
        RouteResponse directions = directionsService.getRoute(origin, destination);

        if (directions == null || directions.getRoutes() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new RouteResponse("Failed to fetch route from Mapbox for customer delivery."));
        }

        return ResponseEntity.ok(directions);
    }
}