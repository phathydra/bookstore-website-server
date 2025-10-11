package com.bookstore.Shipping.entity;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LocationPoint {
    private Double latitude;
    private Double longitude;
    private LocalDateTime timestamp;
}
