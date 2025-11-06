package com.bookstore.Shipping.controller;

import com.bookstore.Shipping.dto.Coordinate;
import com.bookstore.Shipping.dto.RouteResponse;
import com.bookstore.Shipping.dto.WaypointInfo; // <-- IMPORT WaypointInfo
import com.bookstore.Shipping.entity.DeliveryUnit;
// Import Order entity IF your IShippingService can provide it
// import com.bookstore.Shipping.entity.Order;
import com.bookstore.Shipping.service.IDirectionsService;
import com.bookstore.Shipping.service.IGeocodingService;
import com.bookstore.Shipping.service.IShippingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.function.Function; // <-- THÊM MỚI
import java.util.stream.Collectors;

// --- DTOs CHO VIỆC PHÂN CỤM (THÊM MỚI) ---
// Dữ liệu frontend gửi lên
record OrderToCluster(String orderId, String address) {}
record ClusterRequest(List<OrderToCluster> orders, Double maxDistanceKm, Integer minClusterSize) {}

// Dữ liệu backend trả về
record OrderCluster(String clusterName, List<String> orderIds, Coordinate center) {}
record ClusterResponse(List<OrderCluster> clusters, List<String> unclusteredOrderIds) {}

// Dùng để lưu trữ tạm thời sau khi geocode
record GeocodedOrder(String orderId, Coordinate coordinate) {}
// --- KẾT THÚC DTOs ---


@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001", "http://localhost:3002"}, exposedHeaders = "Content-Disposition")
@RestController
@RequestMapping("/api/map")
public class MapController {

    private final IGeocodingService geocodingService;
    private final IDirectionsService directionsService;
    private final IShippingService shippingService;

    // Ngưỡng khoảng cách để quyết định đi thẳng hay qua kho (MET)
    private static final double MAX_DIRECT_DISTANCE_METERS = 150 * 1000; // 150km

    public MapController(IGeocodingService geocodingService, IDirectionsService directionsService, IShippingService shippingService) {
        this.geocodingService = geocodingService;
        this.directionsService = directionsService;
        this.shippingService = shippingService;
    }

    // --- ENDPOINT MỚI ĐỂ PHÂN CỤM (THÊM MỚI) ---

    /**
     * Nhận một danh sách các đơn hàng (ID và địa chỉ),
     * geocode chúng, chạy thuật toán phân cụm (Giả lập DBSCAN),
     * và trả về các cụm tìm được.
     */
    @PostMapping("/cluster-orders")
    public ResponseEntity<ClusterResponse> clusterOrders(@RequestBody ClusterRequest request) {
        if (request.orders() == null || request.orders().isEmpty()) {
            return ResponseEntity.badRequest().body(new ClusterResponse(Collections.emptyList(), Collections.emptyList()));
        }

        // 1. Geocode tất cả các địa chỉ (Đây là bước chậm nhất)
        // Dùng parallelStream() để tăng tốc quá trình geocode
        System.out.println("Bắt đầu geocoding " + request.orders().size() + " đơn hàng...");
        Map<String, GeocodedOrder> geocodedOrdersMap = request.orders().parallelStream()
                .map(order -> {
                    Coordinate coord = geocodeAddressSafe(order.address()); // Dùng hàm helper có sẵn
                    if (coord != null) {
                        return new GeocodedOrder(order.orderId(), coord);
                    }
                    return null; // Bỏ qua nếu geocode thất bại
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(
                        GeocodedOrder::orderId,
                        Function.identity(),
                        (o1, o2) -> o1 // Xử lý nếu có trùng lặp orderId (lấy cái đầu tiên)
                ));

        List<GeocodedOrder> geocodedList = new ArrayList<>(geocodedOrdersMap.values());
        System.out.println("Geocoding thành công " + geocodedList.size() + " đơn hàng.");

        // 2. Chạy thuật toán phân cụm

        // Các tham số
        double epsilon = (request.maxDistanceKm() != null ? request.maxDistanceKm() : 1.0) * 1000; // 1km (đổi ra mét)
        int minPoints = (request.minClusterSize() != null ? request.minClusterSize() : 2); // Tối thiểu 2 đơn/cụm

        // --- GIẢ LẬP LOGIC DBSCAN (bạn nên thay thế bằng thư viện thật) ---
        // (Đây là logic giả lập rất đơn giản, KHÔNG PHẢI DBSCAN)
        List<List<GeocodedOrder>> rawClusters = simpleGreedyCluster(geocodedList, epsilon);
        List<GeocodedOrder> clusteredOrders = rawClusters.stream()
                .flatMap(List::stream)
                .collect(Collectors.toList());
        List<String> unclusteredOrderIds = geocodedList.stream()
                .filter(order -> !clusteredOrders.contains(order))
                .map(GeocodedOrder::orderId)
                .collect(Collectors.toList());
        // --- Kết thúc giả lập ---

        // 3. Định dạng kết quả trả về
        List<OrderCluster> finalClusters = new ArrayList<>();
        int clusterIndex = 1;
        for (List<GeocodedOrder> rawCluster : rawClusters) {
            if (rawCluster.size() >= minPoints) {
                List<String> orderIdsInCluster = rawCluster.stream()
                        .map(GeocodedOrder::orderId)
                        .collect(Collectors.toList());

                Coordinate center = calculateClusterCenter(rawCluster); // Hàm helper tính trung tâm cụm

                String clusterName = String.format("Cụm %d (%d đơn)", clusterIndex++, rawCluster.size());
                finalClusters.add(new OrderCluster(clusterName, orderIdsInCluster, center));
            } else {
                // Thêm các cụm không đủ minPoints vào danh sách unclustered
                unclusteredOrderIds.addAll(rawCluster.stream().map(GeocodedOrder::orderId).collect(Collectors.toList()));
            }
        }

        System.out.println("Phân cụm hoàn tất: " + finalClusters.size() + " cụm, " + unclusteredOrderIds.size() + " đơn lẻ.");
        return ResponseEntity.ok(new ClusterResponse(finalClusters, unclusteredOrderIds));
    }


    // --- Các endpoint khác giữ nguyên ---
    @GetMapping("/route")
    public ResponseEntity<RouteResponse> getRoute(
            @RequestParam String originAddress,
            @RequestParam String destinationAddress) {
        Coordinate originCoord = geocodeAddressSafe(originAddress);
        Coordinate destCoord = geocodeAddressSafe(destinationAddress);

        if (originCoord == null || destCoord == null) {
            System.err.println("Geocoding failed for one or both addresses: [" + originAddress + "], [" + destinationAddress + "]");
            return ResponseEntity.badRequest().body(new RouteResponse("Geocoding failed or address not found."));
        }
        System.out.printf("Origin: lon=%.6f, lat=%.6f%n", originCoord.getLongitude(), originCoord.getLatitude());
        System.out.printf("Destination: lon=%.6f, lat=%.6f%n", destCoord.getLongitude(), destCoord.getLatitude());

        RouteResponse directions = directionsService.getRoute(originCoord, destCoord);
        return ResponseEntity.ok(directions);
    }

    @GetMapping("/route/to-delivery-unit")
    public ResponseEntity<RouteResponse> getRouteToDeliveryUnit(
            @RequestParam double currentLon,
            @RequestParam double currentLat,
            @RequestParam String deliveryUnitId) {
        Optional<String> addressOptional = shippingService.getAddressByDeliveryUnitId(deliveryUnitId);
        if (addressOptional.isEmpty() || addressOptional.get().isBlank() || "null".equalsIgnoreCase(addressOptional.get())) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new RouteResponse("Delivery Unit address not found or not set for ID: " + deliveryUnitId));
        }
        String destinationAddress = addressOptional.get();
        Coordinate destCoord = geocodeAddressSafe(destinationAddress);
        if (destCoord == null) {
            return ResponseEntity.badRequest().body(new RouteResponse("Failed to geocode Delivery Unit address: " + destinationAddress));
        }
        Coordinate origin = new Coordinate(currentLon, currentLat);
        System.out.printf("🚚 Route request: Origin(lon=%.6f, lat=%.6f) -> DU Dest(lon=%.6f, lat=%.6f)%n", currentLon, currentLat, destCoord.getLongitude(), destCoord.getLatitude());
        RouteResponse directions = directionsService.getRoute(origin, destCoord);
        if (directions == null || directions.getRoutes() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new RouteResponse("Failed to fetch route from Mapbox."));
        }
        return ResponseEntity.ok(directions);
    }

    @GetMapping("/route/to-customer-address")
    public ResponseEntity<RouteResponse> getRouteToCustomerAddress(
            @RequestParam double currentLon,
            @RequestParam double currentLat,
            @RequestParam String orderId) {
        String destinationAddress = getFullCustomerAddressForGeocoding(orderId);
        if (destinationAddress.isBlank()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new RouteResponse("Customer address for Order ID " + orderId + " not found or empty."));
        }
        Coordinate destCoord = geocodeAddressSafe(destinationAddress);
        if (destCoord == null) {
            return ResponseEntity.badRequest().body(new RouteResponse("Failed to geocode Customer address: " + destinationAddress));
        }
        Coordinate origin = new Coordinate(currentLon, currentLat);
        System.out.printf("🛵 Route request: Shipper(lon=%.6f, lat=%.6f) -> Customer(lon=%.6f, lat=%.6f) | Address: %s%n", currentLon, currentLat, destCoord.getLongitude(), destCoord.getLatitude(), destinationAddress);
        RouteResponse directions = directionsService.getRoute(origin, destCoord);
        if (directions == null || directions.getRoutes() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new RouteResponse("Failed to fetch route from Mapbox for customer delivery."));
        }
        return ResponseEntity.ok(directions);
    }

    @GetMapping("/delivery-units/coords")
    public ResponseEntity<?> getCoordinatesByUnit(@RequestParam String unit) {
        List<DeliveryUnit> deliveryUnits = shippingService.getDeliveryUnitsByUnit(unit);
        if (deliveryUnits.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Không tìm thấy đơn vị vận chuyển nào có unit = " + unit);
        }
        List<Coordinate> coordinates = deliveryUnits.stream()
                .map(u -> geocodeAddressSafe(u.getBranchAddress()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        return ResponseEntity.ok(coordinates);
    }

    // --- Endpoint Được Sửa Đổi Chính ---
    @GetMapping("/route/optimized-to-customer")
    public ResponseEntity<?> getOptimizedRouteToCustomer(
            @RequestParam String deliveryUnitId,
            @RequestParam String orderId) {
        try {
            final double MAX_DIRECT_DISTANCE_METERS = 150 * 1000; // Ngưỡng 150km

            // 1️⃣ Lấy thông tin & tọa độ kho gốc
            Optional<DeliveryUnit> originUnitOpt = shippingService.getDeliveryUnitById(deliveryUnitId);
            if (originUnitOpt.isEmpty() || originUnitOpt.get().getBranchAddress() == null || originUnitOpt.get().getBranchAddress().isBlank()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("errorMessage", "Không tìm thấy địa chỉ kho xuất phát ID: " + deliveryUnitId));
            }
            DeliveryUnit originUnit = originUnitOpt.get();
            String originAddress = originUnit.getBranchAddress();
            String originName = (originUnit.getName() != null && !originUnit.getName().isBlank()) ? originUnit.getName() : "Kho Gốc ID " + deliveryUnitId.substring(0, 6);
            Coordinate originCoord = geocodeAddressSafe(originAddress);
            if (originCoord == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("errorMessage", "Không thể geocode địa chỉ kho xuất phát: '" + originAddress + "'"));
            }

            // 2️⃣ Lấy thông tin & tọa độ khách hàng
            String fullCustomerAddress = getFullCustomerAddressForGeocoding(orderId);
            String customerIdentifier = getCustomerIdentifier(orderId);
            if (fullCustomerAddress.isBlank()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("errorMessage", "Không tìm thấy địa chỉ khách hàng cho đơn hàng ID: " + orderId));
            }
            Coordinate destCoord = geocodeAddressSafe(fullCustomerAddress);
            if (destCoord == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("errorMessage", "Không thể geocode địa chỉ khách hàng: '" + fullCustomerAddress + "'"));
            }

            // 3️⃣ Tính khoảng cách trực tiếp
            double directDistance = calculateHaversineDistance(originCoord, destCoord);
            System.out.printf("📏 Khoảng cách trực tiếp (ước tính): %.2f km%n", directDistance / 1000.0);

            // 4️⃣ Build WaypointInfo list
            List<WaypointInfo> waypoints = new ArrayList<>();
            waypoints.add(new WaypointInfo(originCoord, originName, "origin")); // Luôn bắt đầu từ kho gốc

            boolean useHubs = directDistance > MAX_DIRECT_DISTANCE_METERS;
            System.out.println("🗺️ Quyết định lộ trình: " + (useHubs ? "Xa (> " + (MAX_DIRECT_DISTANCE_METERS / 1000) + "km) -> Sẽ tìm & sắp xếp CÁC kho trung gian" : "Gần (<= " + (MAX_DIRECT_DISTANCE_METERS / 1000) + "km) -> Đi thẳng"));

            if (useHubs) {
                // Lấy unitName từ trường 'unit' của kho gốc
                String unitName = (originUnit.getUnit() != null && !originUnit.getUnit().isBlank())
                        ? originUnit.getUnit()
                        : "GHTK"; // Fallback nếu trường 'unit' rỗng hoặc null

                System.out.println("Đang tìm kho trung gian cho unit: '" + unitName + "'");

                List<DeliveryUnit> middleUnits = shippingService.getDeliveryUnitsByUnit(unitName);

                System.out.println("Service getDeliveryUnitsByUnit trả về " + middleUnits.size() + " đơn vị cho unit '" + unitName + "'.");

                // Geocode và tạo WaypointInfo cho các kho trung gian hợp lệ
                List<WaypointInfo> allGeocodedHubs = middleUnits.stream()
                        .filter(unit -> !unit.getDeliveryUnitId().equals(deliveryUnitId)) // Loại kho gốc
                        .map(unit -> {
                            Coordinate coord = geocodeAddressSafe(unit.getBranchAddress());
                            String hubName = (unit.getName() != null && !unit.getName().isBlank()) ? unit.getName() : "Kho TG " + unit.getDeliveryUnitId().substring(0, 6);
                            System.out.println("   -> Geocoding kho '" + hubName + "' ("+ unit.getDeliveryUnitId() +"): " + (coord != null ? "OK " + coord.getLatitude()+","+coord.getLongitude() : "FAILED"));
                            return (coord != null) ? new WaypointInfo(coord, hubName, "hub") : null;
                        })
                        .filter(Objects::nonNull) // Lọc những kho geocode lỗi
                        .collect(Collectors.toList());

                System.out.println("🔍 Tìm thấy " + allGeocodedHubs.size() + " kho trung gian hợp lệ (đã geocode).");

                if (!allGeocodedHubs.isEmpty()) {

                    // === LOGIC LỌC KHO MỚI ===
                    List<WaypointInfo> relevantHubs = allGeocodedHubs.stream()
                            .filter(hub -> {
                                double dist_O_H = calculateHaversineDistance(originCoord, hub.getCoordinate());
                                double dist_H_D = calculateHaversineDistance(hub.getCoordinate(), destCoord);
                                boolean isBetween = (dist_O_H < directDistance) && (dist_H_D < directDistance);
                                return isBetween;
                            })
                            .collect(Collectors.toList());
                    System.out.println("   -> Trong đó có " + relevantHubs.size() + " kho nằm trong 'vùng' hợp lệ (giữa gốc và đích).");

                    if (!relevantHubs.isEmpty()) {
                        // 2. Sắp xếp các kho này theo khoảng cách từ GỐC
                        relevantHubs.sort(Comparator.comparingDouble(hub -> calculateHaversineDistance(originCoord, hub.getCoordinate())));

                        System.out.println("   -> Sắp xếp " + relevantHubs.size() + " kho theo khoảng cách từ gốc:");
                        relevantHubs.forEach(hub -> System.out.printf("      - %s (%.2f km từ gốc)%n", hub.getName(), calculateHaversineDistance(originCoord, hub.getCoordinate()) / 1000.0));

                        // 3. Thêm TẤT CẢ các kho đã lọc và sắp xếp vào lộ trình
                        waypoints.addAll(relevantHubs);
                        System.out.println("✅ Đã thêm " + relevantHubs.size() + " kho trung gian vào lộ trình.");

                    } else {
                        System.out.println("⚠️ Không tìm thấy kho trung gian nào trong 'vùng' hợp lệ. Buộc đi thẳng.");
                    }

                } else {
                    System.out.println("⚠️ Không tìm thấy kho trung gian hợp lệ nào (lỗi geocode?). Buộc đi thẳng.");
                }
                // Luôn thêm điểm đích cuối cùng
                waypoints.add(new WaypointInfo(destCoord, customerIdentifier, "destination"));

            } else { // Trường hợp Gần
                waypoints.add(new WaypointInfo(destCoord, customerIdentifier, "destination"));
            }

            // Loại bỏ waypoints trùng lặp liên tiếp
            List<WaypointInfo> finalWaypoints = new ArrayList<>();
            if (!waypoints.isEmpty()) {
                finalWaypoints.add(waypoints.get(0));
                for (int i = 1; i < waypoints.size(); i++) {
                    if (!isSameLocation(waypoints.get(i).getCoordinate(), waypoints.get(i - 1).getCoordinate())) {
                        finalWaypoints.add(waypoints.get(i));
                    } else {
                        System.out.println("🚫 Loại bỏ waypoint trùng lặp: " + waypoints.get(i).getName());
                    }
                }
            }

            // Extract coordinates for Mapbox API call
            List<Coordinate> finalCoordinates = finalWaypoints.stream()
                    .map(WaypointInfo::getCoordinate)
                    .filter(Objects::nonNull) // Ensure no null coordinates
                    .collect(Collectors.toList());

            // 5️⃣ Call Directions API
            if (finalCoordinates.size() < 2) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("errorMessage", "Không đủ điểm hợp lệ để tạo tuyến đường, sau khi lọc còn: " + finalCoordinates.size()));
            }

            System.out.println("➡️ Waypoints cuối cùng (" + finalCoordinates.size() + " điểm) để gọi API: " + finalWaypoints.stream()
                    .map(wp -> String.format(Locale.US, "%s[%.4f, %.4f]", wp.getName(), wp.getCoordinate().getLongitude(), wp.getCoordinate().getLatitude()))
                    .collect(Collectors.joining(" -> ")));

            RouteResponse response;
            if (finalCoordinates.size() <= 25) {
                System.out.println("🚀 Gọi API getOptimizedRoute (" + finalCoordinates.size() + " điểm)");
                response = directionsService.getOptimizedRoute(finalCoordinates);
            } else {
                System.out.println("🚀 Gọi API getOptimizedRouteInBatches (" + finalCoordinates.size() + " điểm)");
                response = directionsService.getOptimizedRouteInBatches(finalCoordinates);
            }

            // 6️⃣ Check response and ADD WaypointInfo list
            if (response == null || response.getRoutes() == null || response.getRoutes().isEmpty() || (response.getErrorMessage() != null && !response.getErrorMessage().isBlank())) {
                String errorMsg = (response != null && response.getErrorMessage() != null) ? response.getErrorMessage() : "Không tìm thấy tuyến đường từ Mapbox.";
                System.err.println("❌ Lỗi từ Mapbox hoặc không có tuyến đường: " + errorMsg);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new RouteResponse(errorMsg));
            }

            // Gán lại danh sách waypoints đầy đủ (với tên) vào response
            response.setWaypoints(finalWaypoints);

            System.out.println("✅ Trả về tuyến đường thành công với " + finalWaypoints.size() + " waypoints.");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("errorMessage", "Lỗi máy chủ khi xử lý yêu cầu tuyến đường: " + e.getMessage()));
        }
    }
    // ============================================
    // === HELPER METHODS =========================
    // ============================================

    /**
     * [GIẢ LẬP] Thuật toán phân cụm tham lam đơn giản. (THÊM MỚI)
     * BẠN NÊN THAY THẾ BẰNG THƯ VIỆN DBSCAN (ví dụ: Apache Commons Math)
     */
    private List<List<GeocodedOrder>> simpleGreedyCluster(List<GeocodedOrder> points, double maxDistanceMeters) {
        List<List<GeocodedOrder>> clusters = new ArrayList<>();
        Set<GeocodedOrder> visited = new HashSet<>();

        for (GeocodedOrder point : points) {
            if (visited.contains(point)) {
                continue;
            }

            List<GeocodedOrder> newCluster = new ArrayList<>();
            newCluster.add(point);
            visited.add(point);

            // Tìm tất cả các điểm lân cận
            for (GeocodedOrder otherPoint : points) {
                if (!visited.contains(otherPoint)) {
                    double distance = calculateHaversineDistance(point.coordinate(), otherPoint.coordinate());
                    if (distance <= maxDistanceMeters) {
                        newCluster.add(otherPoint);
                        visited.add(otherPoint);
                    }
                }
            }
            clusters.add(newCluster);
        }
        return clusters;
    }

    /**
     * Tính toán tọa độ trung tâm (centroid) của một cụm. (THÊM MỚI)
     */
    private Coordinate calculateClusterCenter(List<GeocodedOrder> cluster) {
        if (cluster == null || cluster.isEmpty()) {
            return null;
        }
        double sumLat = 0;
        double sumLon = 0;
        for (GeocodedOrder order : cluster) {
            sumLat += order.coordinate().getLatitude();
            sumLon += order.coordinate().getLongitude();
        }
        return new Coordinate(sumLon / cluster.size(), sumLat / cluster.size());
    }


    private double calculateHaversineDistance(Coordinate c1, Coordinate c2) {
        if (c1 == null || c2 == null) return Double.MAX_VALUE;
        final int R = 6371 * 1000; // Earth radius in meters
        double lat1Rad = Math.toRadians(c1.getLatitude());
        double lat2Rad = Math.toRadians(c2.getLatitude());
        double deltaLat = Math.toRadians(c2.getLatitude() - c1.getLatitude());
        double deltaLon = Math.toRadians(c2.getLongitude() - c1.getLongitude());
        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2) +
                Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                        Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    private Coordinate geocodeAddressSafe(String address) {
        if (address == null || address.isBlank()) return null;
        try {
            String trimmedAddress = address.trim().replaceAll("\\s+", " "); // Thay thế nhiều khoảng trắng bằng 1
            if (trimmedAddress.length() < 5 || "null".equalsIgnoreCase(trimmedAddress)) {
                System.err.println("⚠️ Địa chỉ không hợp lệ hoặc quá ngắn để geocode: '" + address + "'");
                return null;
            }
            double[] coords = geocodingService.getCoordinatesFromAddress(trimmedAddress);
            if (coords != null && coords.length == 2 && (Math.abs(coords[0]) > 1e-6 || Math.abs(coords[1]) > 1e-6)) {
                return new Coordinate(coords[0], coords[1]); // lon, lat
            } else {
                System.err.println("⚠️ Geocoding trả về tọa độ không hợp lệ hoặc (0,0) cho: '" + trimmedAddress + "' -> " + Arrays.toString(coords));
            }
        } catch (RuntimeException e) {
            System.err.println("ℹ️ Không thể geocode địa chỉ: '" + address + "'. Lý do: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("❌ Lỗi không mong muốn khi geocoding: '" + address + "'. Error: " + e.getMessage());
        }
        return null;
    }

    private String getCustomerIdentifier(String orderId) {
        // --- TODO: IMPLEMENT using IShippingService to get Order details ---
        /* Optional<Order> orderOpt = shippingService.getOrderById(orderId); ... return orderOpt.get().getRecipientName(); */
        Optional<String> addressOpt = shippingService.getCustomerAddressByOrderId(orderId); // Fallback
        if (addressOpt.isPresent() && !addressOpt.get().isBlank()) {
            String[] parts = addressOpt.get().split(",");
            if (parts.length > 0 && !parts[0].trim().isBlank()) return parts[0].trim();
            return "Khách hàng";
        }
        return "Khách hàng (ID: " + orderId.substring(0,6) + ")";
    }

    private String getFullCustomerAddressForGeocoding(String orderId) {
        // --- TODO: IMPLEMENT using IShippingService to get Order details and combine address parts ---
        /* Optional<Order> orderOpt = shippingService.getOrderById(orderId); ... return combinedAddress; */
        Optional<String> addressOpt = shippingService.getCustomerAddressByOrderId(orderId); // Fallback
        return addressOpt.orElse("").trim();
    }

    private boolean isSameLocation(Coordinate c1, Coordinate c2) {
        if (c1 == null || c2 == null) return c1 == c2;
        double epsilon = 1e-6;
        return Math.abs(c1.getLatitude() - c2.getLatitude()) < epsilon &&
                Math.abs(c1.getLongitude() - c2.getLongitude()) < epsilon;
    }

} // End of MapController class