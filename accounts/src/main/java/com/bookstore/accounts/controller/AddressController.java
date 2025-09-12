package com.bookstore.accounts.controller;

import com.bookstore.accounts.dto.AddressDto;
import com.bookstore.accounts.entity.Address;
import com.bookstore.accounts.exception.ResourceNotFoundException;
import com.bookstore.accounts.repository.AddressRepository;
import com.bookstore.accounts.service.IAddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@CrossOrigin("*")
@RestController
@RequestMapping("/api/address")
public class AddressController {

    private final IAddressService addressService;
    @Autowired
    private AddressRepository addressRepository;

    public AddressController(IAddressService addressService) {
        this.addressService = addressService;
    }

    @PostMapping("/create")
    public ResponseEntity<?> createAddress(@RequestBody AddressDto addressDto) {
        return addressService.createAddress(addressDto);
    }

    @GetMapping // Xử lý GET /api/address?accountId=...
    public ResponseEntity<List<AddressDto>> getAddressesByAccountId(@RequestParam String accountId) {
        return addressService.getAddressesByAccountId(accountId);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateAddress(@PathVariable String id, @RequestBody AddressDto addressDto) {
        return addressService.updateAddress(id, addressDto);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteAddress(@PathVariable String id) {
        return addressService.deleteAddress(id);
    }
    @PutMapping("/updateStatus/{id}")
    public ResponseEntity<Address> updateAddressStatus(@PathVariable String id, @RequestBody Map<String, String> status) {
        Address address = addressRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Address not found"));
        address.setStatus(status.get("status")); // Cập nhật trạng thái

        addressRepository.save(address); // Lưu địa chỉ đã cập nhật

        return ResponseEntity.ok(address); // Trả lại địa chỉ đã cập nhật
    }

}
