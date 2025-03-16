package com.bookstore.orders.controller;

import com.bookstore.orders.dto.OrderDto;
import com.bookstore.orders.entity.Order;
import com.bookstore.orders.service.IOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = "http://localhost:3001, http://localhost:3000")
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private IOrderService orderService;

    @PostMapping("/create")
    public ResponseEntity<Order> createOrder(@RequestBody OrderDto orderDto) {
        Order createdOrder = orderService.createOrder(orderDto);
        return ResponseEntity.ok(createdOrder);
    }

    // Get orders by accountId
    @GetMapping("/{accountId}")
    public ResponseEntity<List<Order>> getOrdersByAccountId(@PathVariable String accountId) {
        List<Order> orders = orderService.getOrdersByAccountId(accountId);
        return ResponseEntity.ok(orders);
    }

    // Get order by _id (orderId)
    @GetMapping("/orderId/{orderId}")
    public ResponseEntity<Order> getOrderById(@PathVariable String orderId) {
        Optional<Order> order = orderService.getOrderById(orderId);  // Sử dụng Optional để tránh NullPointerException
        return order.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Get all orders
    @GetMapping("")
    public ResponseEntity<Page<Order>> getAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Page<Order> orders = orderService.getAllOrders(page, size);
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Page.empty());
        }
    }

    @PutMapping("/update-shipping-status/{orderId}")
    public ResponseEntity<Order> updateShippingStatus(
            @PathVariable String orderId,
            @RequestParam String shippingStatus) {

        Optional<Order> updatedOrder = orderService.updateShippingStatus(orderId, shippingStatus);

        return updatedOrder.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

}
