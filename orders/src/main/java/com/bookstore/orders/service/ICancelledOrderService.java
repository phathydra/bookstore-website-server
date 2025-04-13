package com.bookstore.orders.service;

import com.bookstore.orders.dto.CancelledOrderDto;
import com.bookstore.orders.entity.CancelledOrder;

import java.util.List;
import java.util.Optional;

public interface ICancelledOrderService {
    CancelledOrder requestCancellation(CancelledOrderDto cancelledOrderDto);
    Optional<CancelledOrder> updateCancellationStatus(String id, String status);
    Optional<CancelledOrder> getCancelledOrderById(String id);
    Optional<CancelledOrder> getCancelledOrderByOrderId(String orderId);

    List<CancelledOrder> getAllCancelledOrders();
}