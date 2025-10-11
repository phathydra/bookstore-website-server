package com.bookstore.Shipping.service.Impl;

import com.bookstore.Shipping.dto.Coordinate;
import com.bookstore.Shipping.dto.MapboxDirectionsResponse;
import com.bookstore.Shipping.dto.RouteResponse;
import com.bookstore.Shipping.service.IDirectionsService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale; // <== IMPORT MỚI
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DirectionsServiceImpl implements IDirectionsService {

    @Value("${mapbox.access.token}")
    private String mapboxToken;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Dùng Geocoding API để lấy tọa độ từ địa chỉ
     */
    private Coordinate getCoordinateFromAddress(String address) {
        try {
            String encoded = URLEncoder.encode(address, StandardCharsets.UTF_8);
            String url = String.format(
                    "https://api.mapbox.com/geocoding/v5/mapbox.places/%s.json?limit=1&access_token=%s",
                    encoded, mapboxToken
            );

            Map response = restTemplate.getForObject(url, Map.class);
            if (response == null || !response.containsKey("features")) {
                throw new RuntimeException("Không tìm thấy tọa độ cho địa chỉ: " + address);
            }

            List features = (List) response.get("features");
            if (features.isEmpty()) {
                throw new RuntimeException("Không tìm thấy tọa độ cho địa chỉ: " + address);
            }

            Map firstFeature = (Map) features.get(0);
            List<Double> center = (List<Double>) firstFeature.get("center");

            double lon = center.get(0); // đúng chuẩn Mapbox: [lon, lat]
            double lat = center.get(1);

            System.out.printf("Geocoded [%s] -> lon: %.6f, lat: %.6f%n", address, lon, lat);

            // Coordinate(longitude, latitude)
            return new Coordinate(lon, lat);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi lấy tọa độ từ địa chỉ '" + address + "': " + e.getMessage(), e);
        }
    }

    /**
     * Trường hợp controller truyền vào địa chỉ string
     */
    public RouteResponse getRouteByAddress(String originAddress, String destinationAddress) {
        Coordinate origin = getCoordinateFromAddress(originAddress);
        Coordinate destination = getCoordinateFromAddress(destinationAddress);
        return getRoute(origin, destination);
    }

    @Override
    public RouteResponse getRoute(Coordinate origin, Coordinate destination) {
        try {
            // Mapbox format: lon,lat
            // SỬ DỤNG Locale.US để đảm bảo dấu thập phân là dấu chấm (.)
            String url = String.format(Locale.US, // <== SỬA ĐỂ BUỘC DẤU CHẤM LÀM DẤU THẬP PHÂN
                    "https://api.mapbox.com/directions/v5/mapbox/driving/%.6f,%.6f;%.6f,%.6f?geometries=geojson&access_token=%s",
                    origin.getLongitude(), origin.getLatitude(),
                    destination.getLongitude(), destination.getLatitude(),
                    mapboxToken
            );

            System.out.println("Mapbox URL: " + url);

            MapboxDirectionsResponse mapboxResponse =
                    restTemplate.getForObject(url, MapboxDirectionsResponse.class);

            if (mapboxResponse == null || mapboxResponse.getRoutes() == null) {
                return new RouteResponse("No routes found from Mapbox.");
            }

            // Convert từ MapboxDirectionsResponse -> RouteResponse
            List<RouteResponse.Route> routeList = mapboxResponse.getRoutes().stream()
                    .map(r -> {
                        RouteResponse.Route rr = new RouteResponse.Route();
                        rr.setDistance(r.getDistance());
                        rr.setDuration(r.getDuration());
                        rr.setGeometry(Map.of(
                                "type", r.getGeometry().getType(),
                                "coordinates", r.getGeometry().getCoordinates()
                        ));
                        return rr;
                    }).collect(Collectors.toList());

            RouteResponse response = new RouteResponse();
            response.setRoutes(routeList);
            return response;

        } catch (Exception e) {
            e.printStackTrace();
            return new RouteResponse("Error fetching directions from Mapbox: " + e.getMessage());
        }
    }
}