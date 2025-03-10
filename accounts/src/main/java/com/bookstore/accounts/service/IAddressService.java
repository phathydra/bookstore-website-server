package com.bookstore.accounts.service;

import com.bookstore.accounts.dto.AddressDto;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface IAddressService {
    ResponseEntity<?> createAddress(AddressDto addressDto);
    ResponseEntity<List<AddressDto>> getAddressesByAccountId(String accountId);
    ResponseEntity<?> updateAddress(String id, AddressDto addressDto);
    ResponseEntity<?> deleteAddress(String id);
}
