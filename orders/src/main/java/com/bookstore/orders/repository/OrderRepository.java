package com.bookstore.orders.repository;

import com.bookstore.orders.entity.Order;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends MongoRepository<Order, String> {
    List<Order> findByAccountIdOrderByDateOrderDesc(String accountId);
}
