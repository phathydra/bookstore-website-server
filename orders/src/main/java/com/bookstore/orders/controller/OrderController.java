// src/main/java/com/bookstore/orders/controller/OrderController.java
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
import com.bookstore.orders.dto.BestSellingBookDto;

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
        Optional<Order> order = orderService.getOrderById(orderId);
        return order.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // --- CẬP NHẬT PHƯƠNG THỨC getAllOrders NÀY ---
    @GetMapping("")
    public ResponseEntity<Page<Order>> getOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String shippingStatus, // Thêm tham số này
            @RequestParam(required = false) String search) { // Thêm tham số này
        try {
            Page<Order> orders = orderService.getFilteredAndSearchedOrders(page, size, shippingStatus, search);
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            // Log lỗi để dễ dàng debug hơn
            System.err.println("Error fetching filtered/searched orders: " + e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Page.empty()); // Trả về Page rỗng nếu có lỗi
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

    @GetMapping("/top-selling")
    public ResponseEntity<List<BestSellingBookDto>> getTopSellingBooks() {
        List<BestSellingBookDto> topBooks = orderService.getTop5BestSellingBooks();
        return ResponseEntity.ok(topBooks);
    }

    @GetMapping("/purchased-books/{accountId}")
    public ResponseEntity<List<BestSellingBookDto>> getPurchasedBooks(@PathVariable String accountId) {
        List<BestSellingBookDto> books = orderService.getPurchasedBooksByAccountId(accountId);
        return ResponseEntity.ok(books);
    }

}