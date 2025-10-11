package com.bookstore.Shipping.service;

import java.awt.*;

public interface IGeocodingService {
    double[] getCoordinatesFromAddress(String address);
}

