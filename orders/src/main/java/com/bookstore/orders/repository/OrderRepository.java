package com.bookstore.orders.repository;

import com.bookstore.orders.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends MongoRepository<Order, String> {
    List<Order> findByAccountIdOrderByDateOrderDesc(String accountId);

    // Lọc theo shippingStatus
    Page<Order> findByShippingStatus(String shippingStatus, Pageable pageable);

    // Tìm kiếm theo recipientName hoặc orderId
    // Đã thay đổi từ IdContaining sang OrderIdContaining
    Page<Order> findByRecipientNameContainingIgnoreCaseOrOrderIdContaining(String recipientName, String orderId, Pageable pageable);

    // Lọc theo shippingStatus VÀ tìm kiếm theo recipientName hoặc orderId
    // Đã thay đổi từ IdContaining sang OrderIdContaining
    Page<Order> findByShippingStatusAndRecipientNameContainingIgnoreCaseOrShippingStatusAndOrderIdContaining(
            String shippingStatus1, String recipientName, String shippingStatus2, String orderId, Pageable pageable);
}