package com.bookstore.orders.service.impl;

import com.bookstore.orders.dto.CancelledOrderDto;
import com.bookstore.orders.entity.CancelledOrder;
import com.bookstore.orders.entity.Order;
import com.bookstore.orders.entity.OrderItem;
import com.bookstore.orders.mapper.CancelledOrderMapper;
import com.bookstore.orders.repository.CancelledOrderRepository;
import com.bookstore.orders.repository.OrderRepository;
import com.bookstore.orders.service.ICancelledOrderService;
import com.bookstore.orders.service.IOrderService; // Import IOrderService
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class CancelledOrderServiceImpl implements ICancelledOrderService {

    @Autowired
    private CancelledOrderRepository cancelledOrderRepository;

    @Autowired
    private CancelledOrderMapper cancelledOrderMapper;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${book-service.base-url}")
    private String bookServiceBaseUrl;

    @Autowired
    private IOrderService orderService; // Autowired IOrderService

    @Override
    public CancelledOrder requestCancellation(CancelledOrderDto cancelledOrderDto) {
        CancelledOrder cancelledOrder = cancelledOrderMapper.toEntity(cancelledOrderDto);
        cancelledOrder.setCancellationStatus("Yêu cầu hủy đơn");
        cancelledOrder.setCancellationDate(LocalDateTime.now());
        return cancelledOrderRepository.save(cancelledOrder);
    }

    @Override
    @Transactional
    public Optional<CancelledOrder> updateCancellationStatus(String id, String status) {
        Optional<CancelledOrder> cancelledOrderOptional = cancelledOrderRepository.findById(id);

        if (cancelledOrderOptional.isPresent()) {
            CancelledOrder cancelledOrder = cancelledOrderOptional.get();
            cancelledOrder.setCancellationStatus(status);
            cancelledOrderRepository.save(cancelledOrder);

            Optional<Order> orderOptional = orderRepository.findById(cancelledOrder.getOrderId());
            if (orderOptional.isPresent()) {
                Order order = orderOptional.get();
                if (status.equals("Đồng ý")) {
                    for (OrderItem item : order.getOrderItems()) {
                        increaseBookStock(item.getBookId(), item.getQuantity()); // Gọi increaseBookStock
                    }
                    orderService.updateShippingStatus(order.getOrderId(), "Đã hủy"); // Gọi orderService
                } else if (status.equals("Từ chối")) {
                    orderService.updateShippingStatus(order.getOrderId(), "Đã nhận đơn"); // Gọi orderService
                }
                return Optional.of(cancelledOrder);
            }
            return Optional.of(cancelledOrder);
        }
        return Optional.empty();
    }

    @Override
    public Optional<CancelledOrder> getCancelledOrderById(String id) {
        return cancelledOrderRepository.findById(id);
    }

    @Override
    public Optional<CancelledOrder> getCancelledOrderByOrderId(String orderId) {
        return cancelledOrderRepository.findByOrderId(orderId);
    }

    @Override
    public List<CancelledOrder> getAllCancelledOrders() {
        return cancelledOrderRepository.findAll();
    }

    private void increaseBookStock(String bookId, int quantity) {
        String url = UriComponentsBuilder.fromHttpUrl(bookServiceBaseUrl)
                .path("/api/book/")
                .path(bookId)
                .path("/increase-stock")  // Sử dụng endpoint increase-stock
                .build()
                .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        IncreaseStockRequest requestEntity = new IncreaseStockRequest(quantity); // Sử dụng IncreaseStockRequest

        try {
            restTemplate.exchange(url, HttpMethod.PUT, new HttpEntity<>(requestEntity, headers), Void.class);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi gọi book service để tăng số lượng sách: " + e.getMessage());
        }
    }

    public static class IncreaseStockRequest { // Thêm class này
        private int quantity;

        public IncreaseStockRequest(int quantity) {
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