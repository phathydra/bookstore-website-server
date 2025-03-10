package com.bookstore.accounts.mapper;

import com.bookstore.accounts.dto.AddressDto;
import com.bookstore.accounts.entity.Address;
import org.springframework.stereotype.Component;

@Component
public class AddressMapper {

    public Address toEntity(AddressDto dto) {
        return new Address(
                dto.getId(),
                dto.getAccountId(),
                dto.getPhoneNumber(),
                dto.getRecipientName(),
                dto.getCountry(),
                dto.getCity(),
                dto.getDistrict(),
                dto.getWard(),
                dto.getNote(),
                dto.getStatus() // Map the status field
        );
    }

    public AddressDto toDto(Address entity) {
        return new AddressDto(
                entity.getId(),
                entity.getAccountId(),
                entity.getPhoneNumber(),
                entity.getRecipientName(),
                entity.getCountry(),
                entity.getCity(),
                entity.getDistrict(),
                entity.getWard(),
                entity.getNote(),
                entity.getStatus() // Map the status field
        );
    }
}
