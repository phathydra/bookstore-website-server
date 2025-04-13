package com.bookstore.orders.controller;

import com.bookstore.orders.dto.CancelledOrderDto;
import com.bookstore.orders.entity.CancelledOrder;
import com.bookstore.orders.service.ICancelledOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = "http://localhost:3001, http://localhost:3000")
@RestController
@RequestMapping("/api/cancelled-orders")
public class CancelledOrderController {

    @Autowired
    private ICancelledOrderService cancelledOrderService;

    @PostMapping("/request")
    public ResponseEntity<CancelledOrder> requestCancellation(@RequestBody CancelledOrderDto cancelledOrderDto) {
        CancelledOrder requestedCancellation = cancelledOrderService.requestCancellation(cancelledOrderDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(requestedCancellation);
    }

    @PutMapping("/update-status/{id}")
    public ResponseEntity<CancelledOrder> updateCancellationStatus(
            @PathVariable String id,
            @RequestParam String status) {
        Optional<CancelledOrder> updatedCancellation = cancelledOrderService.updateCancellationStatus(id, status);
        return updatedCancellation.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Optional<CancelledOrder>> getCancelledOrderById(@PathVariable String id) {
        Optional<CancelledOrder> cancelledOrder = cancelledOrderService.getCancelledOrderById(id);
        return ResponseEntity.ok(cancelledOrder);
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<Optional<CancelledOrder>> getCancelledOrderByOrderId(@PathVariable String orderId) {
        Optional<CancelledOrder> cancelledOrder = cancelledOrderService.getCancelledOrderByOrderId(orderId);
        return ResponseEntity.ok(cancelledOrder);
    }

    @GetMapping("")
    public ResponseEntity<List<CancelledOrder>> getAllCancelledOrders() {
        List<CancelledOrder> cancelledOrders = cancelledOrderService.getAllCancelledOrders();
        return ResponseEntity.ok(cancelledOrders);
    }
}