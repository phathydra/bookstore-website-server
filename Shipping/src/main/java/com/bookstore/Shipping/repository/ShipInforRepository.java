package com.bookstore.Shipping.repository;

import com.bookstore.Shipping.entity.ShipInfor;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShipInforRepository extends MongoRepository<ShipInfor, String> {
    Optional<ShipInfor> findByShipperId(String shipperId);
}


