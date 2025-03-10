package com.bookstore.orders.service.impl;

import com.bookstore.orders.dto.OrderDto;
import com.bookstore.orders.entity.Order;
import com.bookstore.orders.mapper.OrderMapper;
import com.bookstore.orders.repository.OrderRepository;
import com.bookstore.orders.service.IOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OrderServiceImpl implements IOrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderMapper orderMapper;

    @Override
    public Order createOrder(OrderDto orderDto) {
        Order order = orderMapper.toEntity(orderDto);
        return orderRepository.save(order);
    }
}
