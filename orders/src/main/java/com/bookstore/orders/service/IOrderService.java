package com.bookstore.orders.service;

import com.bookstore.orders.dto.BestSellingBookDto;
import com.bookstore.orders.dto.OrderDto;
import com.bookstore.orders.dto.OrderStatusDto;
import com.bookstore.orders.dto.RevenueByMonthDto;
import com.bookstore.orders.entity.Order;
import org.springframework.data.domain.Page;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface IOrderService {
    Order createOrder(OrderDto orderDto);

    List<Order> getOrdersByAccountId(String accountId);

    Optional<Order> getOrderById(String orderId);

    Page<Order> getAllOrders(int page, int size);

    Optional<Order> updateShippingStatus(String orderId, String shippingStatus);

    Page<Order> getFilteredAndSearchedOrders(int page, int size, String shippingStatus, String search);
    List<BestSellingBookDto> getTop5BestSellingBooks(Date startDate, Date endDate);
    List<BestSellingBookDto> getPurchasedBooksByAccountId(String accountId);
    List<OrderStatusDto> getOrderStatusCounts(Date startDate, Date endDate);
    long getTotalOrderCount(Date startDate, Date endDate);
    double getTotalRevenue(Date startDate, Date endDate);
    List<RevenueByMonthDto> getRevenueByMonth(Date startDate, Date endDate);
    long getUniqueCustomerCount(Date startDate, Date endDate);
    List<BestSellingBookDto> getTop5BestSellingCategories(Date startDate, Date endDate);
    Page<BestSellingBookDto> getWorstSellingBooksPaginated(Date startDate, Date endDate, int page, int size);
    Page<BestSellingBookDto> getLowStockAlertsPaginated(int threshold, int page, int size); // THÊM
    Optional<Order> assignDeliveryUnit(String orderId, String deliveryUnitId);
    Page<BestSellingBookDto> getConsistentSellersPaginated(int months, int minAvgMonthlySales, int page, int size);
    Page<Order> getOrdersByDeliveryUnitId(String deliveryUnitId, int page, int size);

    Optional<Order> assignShipper(String orderId, String shipperId);

    Page<Order> getOrdersByShipperId(String shipperId, int page, int size);

    String getFullAddressByOrderId(String orderId);
}
