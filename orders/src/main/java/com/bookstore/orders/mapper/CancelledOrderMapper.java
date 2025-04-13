package com.bookstore.orders.mapper;

import com.bookstore.orders.dto.CancelledOrderDto;
import com.bookstore.orders.entity.CancelledOrder;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

@Component
public class CancelledOrderMapper {

    public CancelledOrder toEntity(CancelledOrderDto dto) {
        CancelledOrder entity = new CancelledOrder();
        entity.setOrderId(dto.getOrderId());
        entity.setCancellationReason(dto.getCancellationReason());
        entity.setCancellationStatus(dto.getCancellationStatus());
        entity.setCancellationDate(LocalDateTime.now()); // Set the cancellation date when converting to entity
        return entity;
    }

    public CancelledOrderDto toDto(CancelledOrder entity) {
        CancelledOrderDto dto = new CancelledOrderDto();
        dto.setOrderId(entity.getOrderId());
        dto.setCancellationReason(entity.getCancellationReason());
        dto.setCancellationStatus(entity.getCancellationStatus());
        dto.setCancellationDate(entity.getCancellationDate());
        return dto;
    }
}