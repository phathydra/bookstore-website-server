package com.bookstore.Shipping.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data // Adds getters, setters, toString, equals, hashCode
@NoArgsConstructor // Adds no-args constructor
@AllArgsConstructor // Adds constructor with all args
public class WaypointInfo {
    private Coordinate coordinate;
    private String name;
    private String type; // e.g., "origin", "hub", "destination"
}