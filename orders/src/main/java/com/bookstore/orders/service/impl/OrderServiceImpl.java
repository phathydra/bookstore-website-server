// src/main/java/com/bookstore/orders/service/impl/OrderServiceImpl.java
package com.bookstore.orders.service.impl;

import com.bookstore.orders.dto.OrderDto;
import com.bookstore.orders.entity.Order;
import com.bookstore.orders.entity.OrderItem;
import com.bookstore.orders.mapper.OrderMapper;
import com.bookstore.orders.repository.OrderRepository;
import com.bookstore.orders.service.ICartService;
import com.bookstore.orders.service.IOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Optional;

@Service
public class OrderServiceImpl implements IOrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ICartService iCartService;
    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${book-service.base-url}")
    private String bookServiceBaseUrl;

    @Override
    @Transactional
    public Order createOrder(OrderDto orderDto) {
        Order order = orderMapper.toEntity(orderDto);
        Order savedOrder = orderRepository.save(order);
        String accountId = savedOrder.getAccountId();

        for (OrderItem item : savedOrder.getOrderItems()) {
            String bookId = item.getBookId();
            int quantity = item.getQuantity();

            String url = UriComponentsBuilder.fromHttpUrl(bookServiceBaseUrl)
                    .path("/api/book/")
                    .path(bookId)
                    .path("/decrease-stock")
                    .build()
                    .toUriString();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            DecreaseStockRequest requestEntity = new DecreaseStockRequest(quantity);

            iCartService.removeItem(accountId, bookId);

            try {
                restTemplate.exchange(url, HttpMethod.PUT, new HttpEntity<>(requestEntity, headers), Void.class);
            } catch (Exception e) {
                throw new RuntimeException("Lỗi khi gọi book service để giảm số lượng sách: " + e.getMessage());
            }
        }

        return savedOrder;
    }

    @Override
    public List<Order> getOrdersByAccountId(String accountId) {
        return orderRepository.findByAccountIdOrderByDateOrderDesc(accountId);
    }

    @Override
    public Optional<Order> getOrderById(String orderId) {
        return orderRepository.findById(orderId);
    }

    @Override
    public Page<Order> getAllOrders(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return orderRepository.findAll(pageable);
    }

    @Override
    public Optional<Order> updateShippingStatus(String orderId, String shippingStatus) {
        Optional<Order> orderOptional = orderRepository.findById(orderId);

        if (orderOptional.isPresent()) {
            Order order = orderOptional.get();
            order.setShippingStatus(shippingStatus);
            orderRepository.save(order);
            return Optional.of(order);
        }

        return Optional.empty();
    }

    @Override
    public Page<Order> getFilteredAndSearchedOrders(int page, int size, String shippingStatus, String search) {
        Pageable pageable = PageRequest.of(page, size);

        boolean hasStatusFilter = shippingStatus != null && !shippingStatus.isEmpty();
        boolean hasSearchQuery = search != null && !search.isEmpty();

        if (hasStatusFilter && hasSearchQuery) {
            return orderRepository.findByShippingStatusAndRecipientNameContainingIgnoreCaseOrShippingStatusAndOrderIdContaining(
                    shippingStatus, search, shippingStatus, search, pageable);
            // ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
            // Đã sửa từ IdContaining sang OrderIdContaining
        } else if (hasStatusFilter) {
            return orderRepository.findByShippingStatus(shippingStatus, pageable);
        } else if (hasSearchQuery) {
            return orderRepository.findByRecipientNameContainingIgnoreCaseOrOrderIdContaining(search, search, pageable);
            // ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
            // Đã sửa từ IdContaining sang OrderIdContaining
        } else {
            return orderRepository.findAll(pageable);
        }
    }

    public static class DecreaseStockRequest {
        private int quantity;

        public DecreaseStockRequest(int quantity) {
            this.quantity = quantity;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }
    }
}