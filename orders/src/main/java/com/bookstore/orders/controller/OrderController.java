// src/main/java/com/bookstore/orders/controller/OrderController.java
package com.bookstore.orders.controller;

import com.bookstore.orders.dto.OrderDto;
import com.bookstore.orders.dto.OrderStatusDto;
import com.bookstore.orders.dto.RevenueByMonthDto;
import com.bookstore.orders.entity.Order;
import com.bookstore.orders.service.IOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import com.bookstore.orders.dto.BestSellingBookDto;

@CrossOrigin(origins = "http://localhost:3001, http://localhost:3000, http://localhost:3002")
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
    public ResponseEntity<List<BestSellingBookDto>> getTopSellingBooks(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date startDate, // THÊM
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date endDate) { // THÊM
        List<BestSellingBookDto> topBooks = orderService.getTop5BestSellingBooks(startDate, endDate); // CẬP NHẬT
        return ResponseEntity.ok(topBooks);
    }

    @GetMapping("/purchased-books/{accountId}")
    public ResponseEntity<List<BestSellingBookDto>> getPurchasedBooks(@PathVariable String accountId) {
        List<BestSellingBookDto> books = orderService.getPurchasedBooksByAccountId(accountId);
        return ResponseEntity.ok(books);
    }

    @GetMapping("/dashboard/order-status")
    public ResponseEntity<List<OrderStatusDto>> getOrderStatusCounts(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date endDate) {
        List<OrderStatusDto> statusCounts = orderService.getOrderStatusCounts(startDate, endDate);
        return ResponseEntity.ok(statusCounts);
    }

    @GetMapping("/dashboard/total-orders")
    public ResponseEntity<Long> getTotalOrderCount(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date endDate) {
        long totalOrders = orderService.getTotalOrderCount(startDate, endDate);
        return ResponseEntity.ok(totalOrders);
    }

    @GetMapping("/dashboard/total-revenue")
    public ResponseEntity<Double> getTotalRevenue(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date endDate) {
        double totalRevenue = orderService.getTotalRevenue(startDate, endDate);
        return ResponseEntity.ok(totalRevenue);
    }

    @GetMapping("/dashboard/revenue-by-month")
    public ResponseEntity<List<RevenueByMonthDto>> getRevenueByMonth(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date endDate) {
        List<RevenueByMonthDto> revenue = orderService.getRevenueByMonth(startDate, endDate);
        return ResponseEntity.ok(revenue);
    }
    @GetMapping("/dashboard/unique-customers")
    public ResponseEntity<Long> getUniqueCustomerCount(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date endDate) {
        long uniqueCustomers = orderService.getUniqueCustomerCount(startDate, endDate);
        return ResponseEntity.ok(uniqueCustomers);
    }

    @GetMapping("/top-selling-categories")
    public ResponseEntity<List<BestSellingBookDto>> getTopSellingCategories(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date startDate, // THÊM
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date endDate) { // THÊM
        List<BestSellingBookDto> topCategories = orderService.getTop5BestSellingCategories(startDate, endDate); // CẬP NHẬT
        return ResponseEntity.ok(topCategories);
    }

    @GetMapping("/worst-selling")
    public ResponseEntity<Page<BestSellingBookDto>> getTopWorstSellingBooks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date endDate) {

        // Gọi Service với các tham số phân trang mới
        Page<BestSellingBookDto> worstBooksPage = orderService.getWorstSellingBooksPaginated(
                startDate, endDate, page, size
        );
        return ResponseEntity.ok(worstBooksPage);
    }

    // THÊM ENDPOINT THỐNG KÊ SÁCH SẮP HẾT HÀNG CÓ PHÂN TRANG
    @GetMapping("/dashboard/low-stock-alerts")
    public ResponseEntity<Page<BestSellingBookDto>> getLowStockAlerts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "20") int threshold) { // Mặc định cảnh báo nếu tồn kho < 20
        Page<BestSellingBookDto> alerts = orderService.getLowStockAlertsPaginated(threshold, page, size);
        return ResponseEntity.ok(alerts);
    }

    // THÊM ENDPOINT THỐNG KÊ SÁCH BÁN CHẠY ĐỀU CÓ PHÂN TRANG
    @GetMapping("/dashboard/consistent-sellers")
    public ResponseEntity<Page<BestSellingBookDto>> getConsistentSellers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "3") int months, // Mặc định 3 tháng
            @RequestParam(defaultValue = "10") int minAvgMonthlySales) { // Mặc định trung bình 10 cuốn/tháng

        Page<BestSellingBookDto> sellers = orderService.getConsistentSellersPaginated(months, minAvgMonthlySales, page, size);
        return ResponseEntity.ok(sellers);
    }
    @PutMapping("/assign-delivery-unit/{orderId}")
    public ResponseEntity<Order> assignDeliveryUnit(
            @PathVariable String orderId,
            @RequestParam String deliveryUnitId) {

        Optional<Order> updatedOrder = orderService.assignDeliveryUnit(orderId, deliveryUnitId);

        return updatedOrder.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/assign-shipper/{orderId}")
    public ResponseEntity<Order> assignShipper(
            @PathVariable String orderId,
            @RequestParam String shipperId) {

        Optional<Order> updatedOrder = orderService.assignShipper(orderId, shipperId);

        return updatedOrder.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }


    @GetMapping("/delivery-unit/{deliveryUnitId}")
    public ResponseEntity<Page<Order>> getOrdersByDeliveryUnitId(
            @PathVariable String deliveryUnitId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Page<Order> orders = orderService.getOrdersByDeliveryUnitId(deliveryUnitId, page, size);
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            System.err.println("Error fetching orders by deliveryUnitId: " + e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Page.empty());
        }
    }

    @GetMapping("/shipper/{shipperId}")
    public ResponseEntity<Page<Order>> getOrdersByShipperId(
            @PathVariable String shipperId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            Page<Order> orders = orderService.getOrdersByShipperId(shipperId, page, size);
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            System.err.println("Error fetching orders by shipperId: " + e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Page.empty());
        }
    }

    @GetMapping("/{orderId}/address")
    public ResponseEntity<String> getOrderAddress(@PathVariable String orderId) {
        String fullAddress = orderService.getFullAddressByOrderId(orderId);
        return ResponseEntity.ok(fullAddress);
    }

}