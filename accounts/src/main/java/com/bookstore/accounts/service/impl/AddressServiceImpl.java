package com.bookstore.accounts.service.impl;

import com.bookstore.accounts.dto.AddressDto;
import com.bookstore.accounts.entity.Address;
import com.bookstore.accounts.mapper.AddressMapper;
import com.bookstore.accounts.repository.AddressRepository;
import com.bookstore.accounts.service.IAddressService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AddressServiceImpl implements IAddressService {

    private final AddressRepository addressRepository;
    private final AddressMapper addressMapper;

    public AddressServiceImpl(AddressRepository addressRepository, AddressMapper addressMapper) {
        this.addressRepository = addressRepository;
        this.addressMapper = addressMapper;
    }

    @Override
    public ResponseEntity<?> createAddress(AddressDto addressDto) {
        Address address = addressMapper.toEntity(addressDto);
        addressRepository.save(address);
        return ResponseEntity.ok("Địa chỉ đã được tạo thành công!");
    }

    @Override
    public ResponseEntity<List<AddressDto>> getAddressesByAccountId(String accountId) {
        List<Address> addresses = addressRepository.findByAccountId(accountId);
        List<AddressDto> addressDtos = addresses.stream()
                .map(addressMapper::toDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(addressDtos);
    }

    @Override
    public ResponseEntity<?> updateAddress(String id, AddressDto addressDto) {
        Optional<Address> optionalAddress = addressRepository.findById(id);

        if (optionalAddress.isPresent()) {
            Address address = optionalAddress.get();
            address.setPhoneNumber(addressDto.getPhoneNumber());
            address.setCountry(addressDto.getCountry());
            address.setCity(addressDto.getCity());
            address.setDistrict(addressDto.getDistrict());
            address.setWard(addressDto.getWard());
            address.setNote(addressDto.getNote());

            addressRepository.save(address);
            return ResponseEntity.ok("Cập nhật địa chỉ thành công!");
        }
        return ResponseEntity.badRequest().body("Không tìm thấy địa chỉ!");
    }

    @Override
    public ResponseEntity<?> deleteAddress(String id) {
        if (addressRepository.existsById(id)) {
            addressRepository.deleteById(id);
            return ResponseEntity.ok("Đã xóa địa chỉ!");
        }
        return ResponseEntity.badRequest().body("Không tìm thấy địa chỉ!");
    }
}
