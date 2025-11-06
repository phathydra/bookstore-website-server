package com.bookstore.Shipping.service.Impl;

import com.bookstore.Shipping.dto.Coordinate;
import com.bookstore.Shipping.dto.MapboxDirectionsResponse;
import com.bookstore.Shipping.dto.RouteResponse;
import com.bookstore.Shipping.service.IDirectionsService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DirectionsServiceImpl implements IDirectionsService {

    @Value("${mapbox.access.token}")
    private String mapboxToken;

    private final RestTemplate restTemplate = new RestTemplate();

    // getCoordinateFromAddress (Used internally by geocodingService usually, keep private if not needed directly)
    // Removed duplicate from here as it should be in GeocodingServiceImpl

    /**
     * Gets route between two addresses (convenience method).
     * Relies on an injected GeocodingService.
     */
    // If you need this, inject IGeocodingService here too, otherwise remove it or rely on MapController's geocoding.
    /*
    @Autowired
    private IGeocodingService geocodingService; // Assuming you inject this

    public RouteResponse getRouteByAddress(String originAddress, String destinationAddress) {
         Coordinate origin = geocodeAddressSafe(originAddress); // Use safe geocoding
         Coordinate destination = geocodeAddressSafe(destinationAddress);
         if (origin == null || destination == null) {
              return new RouteResponse("Geocoding failed for one or both addresses.");
         }
        return getRoute(origin, destination);
    }
    */


    @Override
    public RouteResponse getRoute(Coordinate origin, Coordinate destination) {
        if (origin == null || destination == null) {
            return new RouteResponse("Invalid origin or destination coordinate.");
        }
        try {
            String url = String.format(Locale.US,
                    "https://api.mapbox.com/directions/v5/mapbox/driving/%.6f,%.6f;%.6f,%.6f?geometries=geojson&overview=simplified&access_token=%s",
                    origin.getLongitude(), origin.getLatitude(),
                    destination.getLongitude(), destination.getLatitude(),
                    mapboxToken
            );

            System.out.println("🗺️ Calling Mapbox Directions (getRoute): " + url); // Log URL

            MapboxDirectionsResponse mapboxResponse =
                    restTemplate.getForObject(url, MapboxDirectionsResponse.class);

            if (mapboxResponse == null || mapboxResponse.getRoutes() == null || mapboxResponse.getRoutes().isEmpty()) {
                System.err.println("❌ No routes found from Mapbox for " + origin + " -> " + destination);
                return new RouteResponse("No routes found from Mapbox.");
            }

            List<RouteResponse.Route> routeList = mapboxResponse.getRoutes().stream()
                    .map(r -> {
                        RouteResponse.Route rr = new RouteResponse.Route();
                        rr.setDistance(r.getDistance());
                        rr.setDuration(r.getDuration());
                        // Ensure geometry and coordinates are not null before accessing
                        if (r.getGeometry() != null && r.getGeometry().getCoordinates() != null) {
                            rr.setGeometry(Map.of(
                                    "type", r.getGeometry().getType() != null ? r.getGeometry().getType() : "LineString",
                                    "coordinates", r.getGeometry().getCoordinates()
                            ));
                        } else {
                            System.err.println("⚠️ Mapbox response route missing geometry/coordinates.");
                            rr.setGeometry(Map.of("type", "LineString", "coordinates", List.of())); // Empty geometry
                        }
                        return rr;
                    }).collect(Collectors.toList());

            RouteResponse response = new RouteResponse();
            response.setRoutes(routeList);
            return response;

        } catch (HttpClientErrorException e) {
            System.err.println("❌ HTTP Error fetching directions from Mapbox: " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
            return new RouteResponse("Error " + e.getStatusCode() + " from Mapbox: " + e.getStatusText());
        } catch (Exception e) {
            e.printStackTrace(); // Log full stack trace for unexpected errors
            return new RouteResponse("Unexpected error fetching directions: " + e.getMessage());
        }
    }


    @Override
    public RouteResponse getOptimizedRoute(List<Coordinate> coordinates) {
        if (coordinates == null || coordinates.size() < 2) {
            return new RouteResponse("Cần ít nhất 2 tọa độ hợp lệ.");
        }
        // Filter out any null coordinates just in case
        List<Coordinate> validCoords = coordinates.stream().filter(Objects::nonNull).collect(Collectors.toList());
        if (validCoords.size() < 2) {
            return new RouteResponse("Không đủ tọa độ hợp lệ sau khi lọc.");
        }

        try {
            String coordString = validCoords.stream()
                    .map(c -> String.format(Locale.US, "%.6f,%.6f", c.getLongitude(), c.getLatitude()))
                    .collect(Collectors.joining(";"));

            String url = String.format(
                    Locale.US,
                    "https://api.mapbox.com/directions/v5/mapbox/driving/%s?geometries=geojson&overview=simplified&access_token=%s",
                    coordString,
                    mapboxToken
            );

            System.out.println("🗺️ Calling Mapbox Directions (getOptimizedRoute - Simplified): " + url); // Log URL

            MapboxDirectionsResponse mapboxResponse =
                    restTemplate.getForObject(url, MapboxDirectionsResponse.class);

            if (mapboxResponse == null || mapboxResponse.getRoutes() == null || mapboxResponse.getRoutes().isEmpty()) {
                System.err.println("❌ No optimized routes found from Mapbox for " + validCoords.size() + " waypoints.");
                return new RouteResponse("Không tìm thấy tuyến đường tối ưu từ Mapbox.");
            }

            var routes = mapboxResponse.getRoutes().stream().map(r -> {
                RouteResponse.Route rr = new RouteResponse.Route();
                rr.setDistance(r.getDistance());
                rr.setDuration(r.getDuration());
                if (r.getGeometry() != null && r.getGeometry().getCoordinates() != null) {
                    rr.setGeometry(Map.of(
                            "type", r.getGeometry().getType() != null ? r.getGeometry().getType() : "LineString",
                            "coordinates", r.getGeometry().getCoordinates()
                    ));
                } else {
                    System.err.println("⚠️ Mapbox optimized route missing geometry/coordinates.");
                    rr.setGeometry(Map.of("type", "LineString", "coordinates", List.of()));
                }
                return rr;
            }).collect(Collectors.toList());

            RouteResponse result = new RouteResponse();
            result.setRoutes(routes);
            return result;

        } catch (HttpClientErrorException e) {
            System.err.println("❌ HTTP Error fetching optimized directions: " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
            // Provide more specific feedback for common errors
            if (e.getStatusCode() == HttpStatus.UNPROCESSABLE_ENTITY || e.getStatusCode() == HttpStatus.BAD_REQUEST) {
                // Often due to too many coordinates or invalid coordinates
                return new RouteResponse("Lỗi yêu cầu Mapbox (kiểm tra số lượng/tọa độ điểm): " + e.getStatusText());
            }
            return new RouteResponse("Error " + e.getStatusCode() + " from Mapbox: " + e.getStatusText());
        } catch (Exception e) {
            e.printStackTrace();
            return new RouteResponse("Unexpected error optimizing route: " + e.getMessage());
        }
    }

    @Override
    public RouteResponse getOptimizedRouteInBatches(List<Coordinate> allCoords) {
        if (allCoords == null || allCoords.size() < 2) {
            return new RouteResponse("Cần ít nhất 2 tọa độ hợp lệ cho batching.");
        }
        List<Coordinate> validCoords = allCoords.stream().filter(Objects::nonNull).collect(Collectors.toList());
        if (validCoords.size() < 2) {
            return new RouteResponse("Không đủ tọa độ hợp lệ sau khi lọc cho batching.");
        }

        try {
            List<RouteResponse.Route> combinedRoutes = new ArrayList<>();
            // Use Mapbox limit (25) minus 1 for overlap
            final int MAX_WAYPOINTS_PER_REQUEST = 25;
            final int stepSize = MAX_WAYPOINTS_PER_REQUEST - 1;

            System.out.println("📦 Starting batch route calculation for " + validCoords.size() + " points, batch size <= " + MAX_WAYPOINTS_PER_REQUEST);

            for (int i = 0; i < validCoords.size() - 1; i += stepSize) {
                int end = Math.min(i + MAX_WAYPOINTS_PER_REQUEST, validCoords.size());
                List<Coordinate> batch = validCoords.subList(i, end);

                System.out.printf("  -> Batch %d: Points %d to %d (%d points)%n", (i / stepSize) + 1, i, end - 1, batch.size());

                if (batch.size() >= 2) {
                    RouteResponse partial = getOptimizedRoute(batch); // Call the optimized route for the batch
                    if (partial.getRoutes() != null && !partial.getRoutes().isEmpty()) {
                        combinedRoutes.addAll(partial.getRoutes());
                        System.out.println("     ... Added " + partial.getRoutes().size() + " route segment(s).");
                    } else {
                        System.err.println("     ... Batch failed or returned no routes. Message: " + partial.getErrorMessage());
                        // Decide if you want to stop or continue on batch failure
                        // return new RouteResponse("Lỗi khi xử lý một chặng: " + partial.getErrorMessage()); // Option: Stop on error
                    }
                } else if (i + 1 < validCoords.size()) {
                    // This case should ideally not happen with the loop condition, but good to log
                    System.err.println("     ... Batch size < 2, skipping invalid batch.");
                }

                if (end == validCoords.size()) {
                    break; // Exit loop once the last point is included in 'end'
                }
            }

            if (combinedRoutes.isEmpty()){
                System.err.println("❌ Batch processing completed but resulted in zero route segments.");
                return new RouteResponse("Không thể tạo tuyến đường sau khi xử lý các chặng.");
            }

            RouteResponse merged = new RouteResponse();
            merged.setRoutes(combinedRoutes);
            System.out.println("📦 Batch route calculation finished. Total segments: " + combinedRoutes.size());
            return merged;

        } catch (Exception e) {
            e.printStackTrace();
            return new RouteResponse("Lỗi không mong muốn trong quá trình xử lý chặng: " + e.getMessage());
        }
    }

    // Helper for safe geocoding IF needed within this service (duplicate from controller)
    /*
    private Coordinate geocodeAddressSafe(String address) {
        if (address == null || address.isBlank()) return null;
        try {
            double[] coords = geocodingService.getCoordinatesFromAddress(address.trim());
            if (coords != null && coords.length == 2 && (coords[0] != 0 || coords[1] != 0)) {
                return new Coordinate(coords[0], coords[1]);
            }
        } catch (Exception e) {
            // Log?
        }
        return null;
    }
    */

} // End of DirectionsServiceImpl class