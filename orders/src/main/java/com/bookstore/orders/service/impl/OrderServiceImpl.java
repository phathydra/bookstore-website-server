// src/main/java/com/bookstore/orders/service/impl/OrderServiceImpl.java
package com.bookstore.orders.service.impl;

import com.bookstore.orders.dto.OrderDto;
import com.bookstore.orders.dto.OrderStatusDto;
import com.bookstore.orders.dto.RevenueByMonthDto;
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
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

import com.bookstore.orders.dto.BestSellingBookDto;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.domain.Sort;

import java.util.stream.Collectors;



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

    @Autowired
    private MongoTemplate mongoTemplate;

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

    @Override
    public List<BestSellingBookDto> getTop5BestSellingBooks() {
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("shippingStatus").nin("Chờ xử lý", "Đã hủy")),
                Aggregation.unwind("orderItems"),
                Aggregation.group("orderItems.bookId", "orderItems.bookName")
                        .sum("orderItems.quantity").as("totalSold"),
                Aggregation.sort(Sort.Direction.DESC, "totalSold"),
                Aggregation.project()
                        .and("_id.bookId").as("bookId")
                        .and("_id.bookName").as("bookName")
                        .and("totalSold").as("totalSold")
        );

        AggregationResults<BestSellingBookDto> results = mongoTemplate.aggregate(
                aggregation,
                "orders",
                BestSellingBookDto.class
        );

        return results.getMappedResults();
    }

    @Override
    public List<BestSellingBookDto> getPurchasedBooksByAccountId(String accountId) {
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("accountId").is(accountId)
                        .and("shippingStatus").nin("Chờ xử lý", "Đã hủy")),

                Aggregation.unwind("orderItems"),

                Aggregation.group("orderItems.bookId", "orderItems.bookName")
                        .sum("orderItems.quantity").as("totalSold"),

                Aggregation.project()
                        .and("_id.bookId").as("bookId")
                        .and("_id.bookName").as("bookName")
                        .and("totalSold").as("totalSold")
        );

        AggregationResults<BestSellingBookDto> results = mongoTemplate.aggregate(
                aggregation,
                "orders",
                BestSellingBookDto.class
        );

        return results.getMappedResults();
    }

    @Override
    public List<OrderStatusDto> getOrderStatusCounts(Date startDate, Date endDate) {
        // 1. Tạo một Criteria để lọc theo ngày tháng
        Criteria dateCriteria = new Criteria();
        if (startDate != null && endDate != null) {
            dateCriteria.and("dateOrder").gte(startDate).lte(endDate);
        } else if (startDate != null) {
            dateCriteria.and("dateOrder").gte(startDate);
        } else if (endDate != null) {
            dateCriteria.and("dateOrder").lte(endDate);
        }

        // 2. Thêm một bước aggregation "match" nếu có điều kiện lọc
        MatchOperation matchOperation = Aggregation.match(dateCriteria);

        // 3. Nhóm các tài liệu theo trường 'shippingStatus'
        GroupOperation groupOperation = Aggregation.group("shippingStatus")
                .count().as("count");

        // 4. Định hình lại đầu ra
        ProjectionOperation projectionOperation = Aggregation.project()
                .and("_id").as("status")
                .and("count").as("count");

        // 5. Kết hợp các bước aggregation
        Aggregation aggregation = Aggregation.newAggregation(
                matchOperation, // Thêm bước lọc vào đầu pipeline
                groupOperation,
                projectionOperation
        );

        // 6. Thực thi aggregation
        AggregationResults<OrderStatusDto> results = mongoTemplate.aggregate(
                aggregation,
                "orders",
                OrderStatusDto.class
        );

        return results.getMappedResults();
    }

    @Override
    public long getTotalOrderCount(Date startDate, Date endDate) {
        // TẠO MỘT ĐỐI TƯỢNG Query TRỐNG
        Query query = new Query();

        // TẠO MỘT ĐỐI TƯỢNG Criteria ĐỂ CHỨA CÁC ĐIỀU KIỆN
        Criteria criteria = new Criteria();
        if (startDate != null && endDate != null) {
            criteria.and("dateOrder").gte(startDate).lte(endDate);
        } else if (startDate != null) {
            criteria.and("dateOrder").gte(startDate);
        } else if (endDate != null) {
            criteria.and("dateOrder").lte(endDate);
        }

        // Gắn đối tượng Criteria đã xây dựng vào Query
        query.addCriteria(criteria);

        return mongoTemplate.count(query, Order.class);
    }

    @Override
    public double getTotalRevenue(Date startDate, Date endDate) {
        // 1. Bước Match: Lọc các tài liệu theo khoảng thời gian
        Criteria dateCriteria = new Criteria();
        if (startDate != null && endDate != null) {
            dateCriteria.and("dateOrder").gte(startDate).lte(endDate);
        } else if (startDate != null) {
            dateCriteria.and("dateOrder").gte(startDate);
        } else if (endDate != null) {
            dateCriteria.and("dateOrder").lte(endDate);
        }
        MatchOperation matchOperation = Aggregation.match(dateCriteria);

        // 2. Bước Group: Nhóm tất cả các tài liệu đã lọc thành một nhóm duy nhất
        // và tính tổng giá trị của trường 'totalPrice'
        GroupOperation groupOperation = Aggregation.group()
                .sum("totalPrice").as("totalRevenue");

        // 3. Xây dựng pipeline aggregation
        Aggregation aggregation = Aggregation.newAggregation(
                matchOperation,
                groupOperation
        );

        // 4. Thực thi aggregation và lấy kết quả
        AggregationResults<Document> results = mongoTemplate.aggregate(
                aggregation,
                "orders",
                Document.class
        );

        // 5. Trích xuất giá trị tổng doanh thu
        Document resultDocument = results.getUniqueMappedResult();
        if (resultDocument != null && resultDocument.containsKey("totalRevenue")) {
            return resultDocument.getDouble("totalRevenue");
        }

        return 0.0; // Trả về 0 nếu không có kết quả
    }

    @Override
    public List<RevenueByMonthDto> getRevenueByMonth(Date startDate, Date endDate) {
        // XỬ LÝ TRƯỜNG HỢP startDate VÀ endDate LÀ null
        LocalDate startLocalDate;
        LocalDate endLocalDate;

        if (startDate != null && endDate != null) {
            startLocalDate = startDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            endLocalDate = endDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        } else {
            // Nếu không có ngày được chọn, lấy dữ liệu từ đầu năm đến ngày hiện tại
            startLocalDate = LocalDate.now().withDayOfYear(1);
            endLocalDate = LocalDate.now();
        }

        // 1. Tạo một danh sách đầy đủ tất cả các tháng giữa startDate và endDate, với doanh thu = 0
        List<RevenueByMonthDto> fullMonthList = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");

        LocalDate current = startLocalDate.withDayOfMonth(1);
        while (!current.isAfter(endLocalDate)) {
            fullMonthList.add(new RevenueByMonthDto(current.format(formatter), 0.0));
            current = current.plusMonths(1);
        }

        // 2. Chạy pipeline aggregation để lấy doanh thu từ MongoDB
        Criteria dateCriteria = new Criteria();
        if (startDate != null && endDate != null) {
            dateCriteria.and("dateOrder").gte(startDate).lte(endDate);
        } else if (startDate != null) {
            dateCriteria.and("dateOrder").gte(startDate);
        } else if (endDate != null) {
            dateCriteria.and("dateOrder").lte(endDate);
        }
        MatchOperation matchOperation = Aggregation.match(dateCriteria);

        ProjectionOperation projectDates = Aggregation.project()
                .and("totalPrice").as("totalRevenue")
                .and(DateOperators.DateToString.dateOf("dateOrder").toString("%Y-%m")).as("period");

        GroupOperation groupOperation = Aggregation.group("period")
                .sum("totalRevenue").as("totalRevenue");

        Aggregation aggregation = Aggregation.newAggregation(
                matchOperation,
                projectDates,
                groupOperation
        );

        AggregationResults<Document> results = mongoTemplate.aggregate(
                aggregation,
                "orders",
                Document.class
        );

        Map<String, Double> revenueMap = results.getMappedResults().stream()
                .collect(Collectors.toMap(
                        doc -> doc.getString("_id"),
                        doc -> doc.getDouble("totalRevenue")
                ));

        // 3. Cập nhật doanh thu cho danh sách đầy đủ
        fullMonthList.forEach(monthDto -> {
            if (revenueMap.containsKey(monthDto.getPeriod())) {
                monthDto.setTotalRevenue(revenueMap.get(monthDto.getPeriod()));
            }
        });

        return fullMonthList;
    }

    @Override
    public long getUniqueCustomerCount(Date startDate, Date endDate) {
        // 1. Match: Lọc các đơn hàng theo khoảng thời gian
        Criteria dateCriteria = new Criteria();
        if (startDate != null && endDate != null) {
            dateCriteria.and("dateOrder").gte(startDate).lte(endDate);
        } else if (startDate != null) {
            dateCriteria.and("dateOrder").gte(startDate);
        } else if (endDate != null) {
            dateCriteria.and("dateOrder").lte(endDate);
        }
        MatchOperation matchOperation = Aggregation.match(dateCriteria);

        // 2. Group: Nhóm theo tên trường chính xác: "accountId" (chữ 'a' thường)
        GroupOperation uniqueIdGroup = Aggregation.group("accountId");

        // 3. Count: Đếm số lượng khách hàng duy nhất
        GroupOperation countGroup = Aggregation.group().count().as("uniqueCustomerCount");

        // Xây dựng pipeline aggregation
        Aggregation aggregation = Aggregation.newAggregation(
                matchOperation,
                uniqueIdGroup,
                countGroup
        );

        // Thực thi aggregation
        AggregationResults<Document> results = mongoTemplate.aggregate(
                aggregation,
                "orders", // Tên collection của bạn
                Document.class
        );

        // Lấy kết quả
        Document result = results.getUniqueMappedResult();
        if (Objects.nonNull(result) && Objects.nonNull(result.get("uniqueCustomerCount"))) {
            return ((Number) result.get("uniqueCustomerCount")).longValue();
        }
        return 0;
    }
}