package com.bookstore.Shipping.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
public class RouteResponse {
    private List<Route> routes;
    private String errorMessage;

    public RouteResponse(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    @Data
    public static class Route {
        private double distance;
        private double duration;
        private Map<String, Object> geometry;
    }
}
