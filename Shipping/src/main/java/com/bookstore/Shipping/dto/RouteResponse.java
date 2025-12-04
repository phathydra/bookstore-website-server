package com.bookstore.Shipping.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.Map;

@Data // Includes @NoArgsConstructor implicitly if no other constructor is defined
public class RouteResponse {
    private List<Route> routes;
    private String errorMessage;
    private List<WaypointInfo> waypoints; // <-- Correctly uses WaypointInfo

    // Keep constructor for error messages
    public RouteResponse(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    // Default constructor needed if using @Data and defining another constructor
    public RouteResponse() {}

    @Data // Lombok for inner class too
    public static class Route {
        private double distance;
        private double duration;
        private Map<String, Object> geometry;
    }
}