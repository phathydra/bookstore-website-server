package com.bookstore.orders.mapper;

import com.bookstore.orders.dto.OrderDto;
import com.bookstore.orders.dto.OrderItemDto;
import com.bookstore.orders.entity.Order;
import com.bookstore.orders.entity.OrderItem;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.stream.Collectors;
import java.time.LocalDateTime;  // Import LocalDateTime

@Component
public class OrderMapper {

    public Order toEntity(OrderDto dto) {
        Order order = new Order();
        order.setAccountId(dto.getAccountId());
        order.setPhoneNumber(dto.getPhoneNumber());
        order.setRecipientName(dto.getRecipientName());
        order.setCountry(dto.getCountry());
        order.setCity(dto.getCity());
        order.setDistrict(dto.getDistrict());
        order.setWard(dto.getWard());
        order.setNote(dto.getNote());
        order.setTotalPrice(dto.getTotalPrice());
        order.setOrderStatus(dto.getOrderStatus());
        order.setShippingStatus(dto.getShippingStatus());
        order.setPaymentMethod(dto.getPaymentMethod());

        order.setDateOrder(LocalDateTime.now());  // Automatically set the current date and time

        List<OrderItem> items = dto.getOrderItems().stream().map(this::toEntity).collect(Collectors.toList());
        order.setOrderItems(items);

        return order;
    }

    public OrderItem toEntity(OrderItemDto dto) {
        OrderItem item = new OrderItem();
        item.setBookId(dto.getBookId());
        item.setBookName(dto.getBookName());
        item.setBookImage(dto.getBookImage());
        item.setQuantity(dto.getQuantity());
        item.setPrice(dto.getPrice());
        return item;
    }
}
