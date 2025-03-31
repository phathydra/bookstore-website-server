package com.bookstore.orders.repository;

import com.bookstore.orders.entity.OrderVoucher;
import com.bookstore.orders.entity.Voucher;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderVoucherDto extends MongoRepository<OrderVoucher, String> {
    Optional<OrderVoucher> findByOrderId(String orderId);

    List<OrderVoucher> findByVoucherId(String id);
}
