package com.bookstore.accounts.repository;

import com.bookstore.accounts.entity.Address;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface AddressRepository extends MongoRepository<Address, String> {
    List<Address> findByAccountId(String accountId);
}
