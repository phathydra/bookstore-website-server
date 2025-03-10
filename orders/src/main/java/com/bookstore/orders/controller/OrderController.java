package com.bookstore.orders.controller;

import com.bookstore.orders.dto.OrderDto;
import com.bookstore.orders.entity.Order;
import com.bookstore.orders.service.IOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private IOrderService orderService;

    // Enable CORS for this specific endpoint
    @CrossOrigin(origins = "http://localhost:3001")  // Allow requests from frontend (React app)
    @PostMapping("/create")
    public ResponseEntity<Order> createOrder(@RequestBody OrderDto orderDto) {
        Order createdOrder = orderService.createOrder(orderDto);
        return ResponseEntity.ok(createdOrder);
    }

    // Get orders by accountId
    @CrossOrigin(origins = "http://localhost:3001")  // Allow requests from frontend (React app)
    @GetMapping("/{accountId}")
    public ResponseEntity<List<Order>> getOrdersByAccountId(@PathVariable String accountId) {
        List<Order> orders = orderService.getOrdersByAccountId(accountId);
        return ResponseEntity.ok(orders);
    }

    // Get order by _id (orderId)
    @CrossOrigin(origins = "http://localhost:3001")  // Allow requests from frontend (React app)
    @GetMapping("/orderId/{orderId}")
    public ResponseEntity<Order> getOrderById(@PathVariable String orderId) {
        Optional<Order> order = orderService.getOrderById(orderId);  // Sử dụng Optional để tránh NullPointerException
        return order.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
