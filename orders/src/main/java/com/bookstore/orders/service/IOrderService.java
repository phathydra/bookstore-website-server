package com.bookstore.orders.service;

import com.bookstore.orders.dto.OrderDto;
import com.bookstore.orders.entity.Order;

import java.util.List;
import java.util.Optional;

public interface IOrderService {
    Order createOrder(OrderDto orderDto);

    List<Order> getOrdersByAccountId(String accountId);

    Optional<Order> getOrderById(String orderId);
}
