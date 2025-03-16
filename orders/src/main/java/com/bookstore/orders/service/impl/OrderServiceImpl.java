package com.bookstore.orders.service.impl;

import com.bookstore.orders.dto.OrderDto;
import com.bookstore.orders.entity.Order;
import com.bookstore.orders.mapper.OrderMapper;
import com.bookstore.orders.repository.OrderRepository;
import com.bookstore.orders.service.IOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

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

    @Override
    public List<Order> getOrdersByAccountId(String accountId) {
        return orderRepository.findByAccountId(accountId);  // Fetch orders by accountId
    }

    @Override
    public Optional<Order> getOrderById(String orderId) {
        return orderRepository.findById(orderId);  // Tìm đơn hàng theo orderId
    }

    @Override
    public Page<Order> getAllOrders(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return orderRepository.findAll(pageable);
    }

    @Override
    public Optional<Order> updateShippingStatus(String orderId, String shippingStatus) {
        Optional<Order> orderOptional = orderRepository.findById(orderId);

        if (orderOptional.isPresent()) {
            Order order = orderOptional.get();
            order.setShippingStatus(shippingStatus); // Giả sử Order có trường shippingStatus
            orderRepository.save(order);
            return Optional.of(order);
        }

        return Optional.empty();
    }

}
