package com.bookstore.orders.repository;

import com.bookstore.orders.entity.CancelledOrder;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CancelledOrderRepository extends MongoRepository<CancelledOrder, String> {
    Optional<CancelledOrder> findByOrderId(String orderId);
}