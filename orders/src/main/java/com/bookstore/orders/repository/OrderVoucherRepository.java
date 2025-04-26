package com.bookstore.orders.repository;

import com.bookstore.orders.entity.OrderVoucher;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderVoucherRepository extends MongoRepository<OrderVoucher, String> {
    Optional<OrderVoucher> findByOrderId(String orderId);

    List<OrderVoucher> findByVoucherCode(String code);
}
