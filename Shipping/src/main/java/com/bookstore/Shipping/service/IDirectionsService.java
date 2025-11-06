package com.bookstore.Shipping.service;

import com.bookstore.Shipping.dto.Coordinate;
import com.bookstore.Shipping.dto.RouteResponse;

import java.util.List;

public interface IDirectionsService {
    RouteResponse getRoute(Coordinate origin, Coordinate destination);
    RouteResponse getOptimizedRoute(List<Coordinate> coordinates);

    RouteResponse getOptimizedRouteInBatches(List<Coordinate> validWarehouses);
}
