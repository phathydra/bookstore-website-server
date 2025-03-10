package com.bookstore.orders.service;

import com.bookstore.orders.dto.OrderDto;
import com.bookstore.orders.entity.Order;

public interface IOrderService {
    Order createOrder(OrderDto orderDto);
}
