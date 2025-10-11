package com.bookstore.Shipping.repository;

import com.bookstore.Shipping.entity.Shipping;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShippingRepository extends MongoRepository<Shipping, String> {
    List<Shipping> findByRole(String role);
    boolean existsByEmail(String email);
}
