package com.bookstore.orders.service;

import com.bookstore.orders.dto.BestSellingBookDto;
import com.bookstore.orders.dto.OrderDto;
import com.bookstore.orders.entity.Order;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;

public interface IOrderService {
    Order createOrder(OrderDto orderDto);

    List<Order> getOrdersByAccountId(String accountId);

    Optional<Order> getOrderById(String orderId);

    Page<Order> getAllOrders(int page, int size);

    Optional<Order> updateShippingStatus(String orderId, String shippingStatus);

    Page<Order> getFilteredAndSearchedOrders(int page, int size, String shippingStatus, String search);
    List<BestSellingBookDto> getTop5BestSellingBooks();
    List<BestSellingBookDto> getPurchasedBooksByAccountId(String accountId);

}
