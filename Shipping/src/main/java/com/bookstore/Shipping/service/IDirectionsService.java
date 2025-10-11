package com.bookstore.Shipping.service;

import com.bookstore.Shipping.dto.Coordinate;
import com.bookstore.Shipping.dto.RouteResponse;

public interface IDirectionsService {
    RouteResponse getRoute(Coordinate origin, Coordinate destination);
}
