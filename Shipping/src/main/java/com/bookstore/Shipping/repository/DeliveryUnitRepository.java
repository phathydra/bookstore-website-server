package com.bookstore.Shipping.repository;

import com.bookstore.Shipping.entity.DeliveryUnit;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeliveryUnitRepository extends MongoRepository<DeliveryUnit, String> {
    boolean existsByName(String name);
    boolean existsByEmail(String email);
    Optional<DeliveryUnit> findByDeliveryUnitId(String deliveryUnitId);
    List<DeliveryUnit> findByUnit(String unit);
}