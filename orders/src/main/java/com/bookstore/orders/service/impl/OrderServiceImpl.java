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
import org.springframework.data.domain.*;
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

import java.util.stream.Collectors;
import com.fasterxml.jackson.annotation.JsonProperty;


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

    // --- Helper DTOs for RestTemplate communication ---

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

    // ĐÃ SỬA: Thêm Constructor và @JsonProperty để ánh xạ tồn kho
    public static class BookDetailDto {
        private String id;
        private String bookName;
        private String categoryName;

        // SỬA LỖI TỒN KHO: Ánh xạ từ 'bookStockQuantity' sang 'stockQuantity'
        @JsonProperty("bookStockQuantity")
        private int stockQuantity;

        // 1. NO-ARGS CONSTRUCTOR (Cần cho Jackson/RestTemplate)
        public BookDetailDto() {}

        // 2. CONSTRUCTOR 2 THAM SỐ (Sửa lỗi 'Cannot resolve constructor')
        public BookDetailDto(String id, String bookName) {
            this.id = id;
            this.bookName = bookName;
        }

        // 3. CONSTRUCTOR ALL-ARGS (Nếu cần khởi tạo đầy đủ)
        public BookDetailDto(String id, String bookName, String categoryName, int stockQuantity) {
            this.id = id;
            this.bookName = bookName;
            this.categoryName = categoryName;
            this.stockQuantity = stockQuantity;
        }

        // Getters và Setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getBookName() { return bookName; }
        public void setBookName(String bookName) { this.bookName = bookName; }
        public String getCategoryName() { return categoryName; }
        public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
        public int getStockQuantity() { return stockQuantity; }
        public void setStockQuantity(int stockQuantity) { this.stockQuantity = stockQuantity; }
    }

    // --- End Helper DTOs ---


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
        } else if (hasStatusFilter) {
            return orderRepository.findByShippingStatus(shippingStatus, pageable);
        } else if (hasSearchQuery) {
            return orderRepository.findByRecipientNameContainingIgnoreCaseOrOrderIdContaining(search, search, pageable);
        } else {
            return orderRepository.findAll(pageable);
        }
    }


    @Override
    public List<BestSellingBookDto> getTop5BestSellingBooks(Date startDate, Date endDate) {
        // 1. Xây dựng Criteria lọc theo ngày
        Criteria dateCriteria = new Criteria();
        if (startDate != null && endDate != null) {
            dateCriteria.and("dateOrder").gte(startDate).lte(endDate);
        } else if (startDate != null) {
            dateCriteria.and("dateOrder").gte(startDate);
        } else if (endDate != null) {
            dateCriteria.and("dateOrder").lte(endDate);
        }

        // 2. Criteria cho trạng thái đơn hàng (luôn cố định)
        Criteria statusCriteria = Criteria.where("shippingStatus").nin("Chờ xử lý", "Đã hủy");

        // 3. Kết hợp 2 Criteria thành MatchOperation
        Criteria combinedCriteria = new Criteria().andOperator(dateCriteria, statusCriteria);
        MatchOperation matchOperation = Aggregation.match(combinedCriteria);

        Aggregation aggregation = Aggregation.newAggregation(
                matchOperation,
                Aggregation.unwind("orderItems"),
                Aggregation.group("orderItems.bookId", "orderItems.bookName")
                        .sum("orderItems.quantity").as("totalSold"),
                Aggregation.sort(Sort.Direction.DESC, "totalSold"),
                Aggregation.limit(5),
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
                matchOperation,
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

    @Override
    public List<BestSellingBookDto> getTop5BestSellingCategories(Date startDate, Date endDate) {
        // 1. Xây dựng Criteria lọc theo ngày và trạng thái
        Criteria dateCriteria = new Criteria();
        if (startDate != null && endDate != null) {
            dateCriteria.and("dateOrder").gte(startDate).lte(endDate);
        } else if (startDate != null) {
            dateCriteria.and("dateOrder").gte(startDate);
        } else if (endDate != null) {
            dateCriteria.and("dateOrder").lte(endDate);
        }
        Criteria statusCriteria = Criteria.where("shippingStatus").nin("Chờ xử lý", "Đã hủy");
        Criteria combinedCriteria = new Criteria().andOperator(dateCriteria, statusCriteria);
        MatchOperation matchOperation = Aggregation.match(combinedCriteria);

        // --- BƯỚC 1: Lọc và Nhóm theo bookId để lấy tổng số lượng bán được của MỖI sách ---
        Aggregation bookSalesAggregation = Aggregation.newAggregation(
                matchOperation,
                Aggregation.unwind("orderItems"),
                Aggregation.group("orderItems.bookId")
                        .sum("orderItems.quantity").as("totalSold"),
                Aggregation.project()
                        .and("_id").as("bookId")
                        .and("totalSold").as("totalSold")
        );

        AggregationResults<Document> bookSalesResults = mongoTemplate.aggregate(
                bookSalesAggregation,
                "orders",
                Document.class
        );

        List<Document> salesByBook = bookSalesResults.getMappedResults();
        if (salesByBook.isEmpty()) {
            return Collections.emptyList();
        }

        // 2. Lấy danh sách tất cả các bookId duy nhất
        List<String> bookIds = salesByBook.stream()
                .map(doc -> doc.getString("bookId"))
                .collect(Collectors.toList());

        // 3. Gọi Book Service để lấy Category Name cho tất cả các sách
        String url = bookServiceBaseUrl + "/api/book/details-by-ids";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<List<String>> requestEntity = new HttpEntity<>(bookIds, headers);

        BookDetailDto[] bookDetailsArray;
        try {
            bookDetailsArray = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    requestEntity,
                    BookDetailDto[].class
            ).getBody();
        } catch (Exception e) {
            System.err.println("Lỗi khi gọi Book Service để lấy chi tiết sách: " + e.getMessage());
            return Collections.emptyList();
        }

        if (bookDetailsArray == null) {
            return Collections.emptyList();
        }

        Map<String, String> bookIdToCategoryMap = Arrays.stream(bookDetailsArray)
                .collect(Collectors.toMap(BookDetailDto::getId, BookDetailDto::getCategoryName));


        // 4. Nhóm theo Category và tính tổng số lượng bán
        Map<String, Long> salesByCategory = new HashMap<>();

        for (Document doc : salesByBook) {
            String bookId = doc.getString("bookId");
            Long totalSold = ((Number) doc.get("totalSold")).longValue();
            String categoryName = bookIdToCategoryMap.get(bookId);

            if (categoryName != null) {
                salesByCategory.merge(categoryName, totalSold, Long::sum);
            }
        }

        // 5. Chuyển đổi sang DTO, sắp xếp và lấy Top 5
        return salesByCategory.entrySet().stream()
                .map(entry -> new BestSellingBookDto(
                        null,
                        entry.getKey(),
                        entry.getValue().intValue()))
                .sorted(Comparator.comparing(BestSellingBookDto::getTotalSold).reversed())
                .limit(5)
                .collect(Collectors.toList());
    }

    @Override
    public Page<BestSellingBookDto> getWorstSellingBooksPaginated(Date startDate, Date endDate, int page, int size) {

        // 1. TÍNH TOÁN SỐ LIỆU BÁN HÀNG TỪ MONGO (KHÔNG THAY ĐỔI)
        // Xây dựng MatchOperation
        Criteria dateCriteria = new Criteria();
        if (startDate != null && endDate != null) {
            dateCriteria.and("dateOrder").gte(startDate).lte(endDate);
        } else if (startDate != null) {
            dateCriteria.and("dateOrder").gte(startDate);
        } else if (endDate != null) {
            dateCriteria.and("dateOrder").lte(endDate);
        }
        Criteria statusCriteria = Criteria.where("shippingStatus").nin("Chờ xử lý", "Đã hủy");
        Criteria combinedCriteria = new Criteria().andOperator(dateCriteria, statusCriteria);
        MatchOperation matchOperation = Aggregation.match(combinedCriteria);

        // Aggregation chỉ để lấy tổng số lượng bán trong khoảng thời gian
        Aggregation bookSalesAggregation = Aggregation.newAggregation(
                matchOperation,
                Aggregation.unwind("orderItems"),
                Aggregation.group("orderItems.bookId", "orderItems.bookName")
                        .sum("orderItems.quantity").as("totalSold"),
                Aggregation.project()
                        .and("_id.bookId").as("bookId")
                        .and("_id.bookName").as("bookName")
                        .and("totalSold").as("totalSold")
        );

        AggregationResults<Document> bookSalesResults = mongoTemplate.aggregate(
                bookSalesAggregation,
                "orders",
                Document.class
        );

        Map<String, Document> salesMap = bookSalesResults.getMappedResults().stream()
                .collect(Collectors.toMap(
                        doc -> doc.getString("bookId"),
                        doc -> doc
                ));

        // 2. GỌI BOOK SERVICE LẤY TẤT CẢ SÁCH (Sử dụng logic RestTemplate như cũ)
        String url = bookServiceBaseUrl + "/api/book/all-details";
        BookDetailDto[] allBooksArray;
        try {
            allBooksArray = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    BookDetailDto[].class
            ).getBody();
        } catch (Exception e) {
            System.err.println("Lỗi khi gọi Book Service: " + e.getMessage());
            return Page.empty();
        }

        if (allBooksArray == null) {
            return Page.empty();
        }

        // 3. KẾT HỢP DỮ LIỆU, LỌC SÁCH BÁN DƯỚI 10, SẮP XẾP
        List<BestSellingBookDto> allWorstSellingBooks = Arrays.stream(allBooksArray)
                .map(bookDetail -> {
                    String bookId = bookDetail.getId();
                    String bookName = bookDetail.getBookName();

                    Document salesDoc = salesMap.get(bookId);
                    long totalSold = 0;

                    if (salesDoc != null && salesDoc.containsKey("totalSold")) {
                        totalSold = ((Number) salesDoc.get("totalSold")).longValue();
                    }

                    return new BestSellingBookDto(bookId, bookName, (int) totalSold);
                })
                .filter(dto -> dto.getTotalSold() < 10) // LỌC: Sách bán dưới 10 cuốn
                .sorted(Comparator.comparing(BestSellingBookDto::getTotalSold)) // Sắp xếp TĂNG DẦN
                .collect(Collectors.toList());

        // 4. PHÂN TRANG THỦ CÔNG (IN-MEMORY PAGING)
        int totalBooks = allWorstSellingBooks.size();
        Pageable pageable = PageRequest.of(page, size);

        int start = (int) pageable.getOffset();
        int end = Math.min((start + size), totalBooks);

        if (start > totalBooks) {
            // Nếu trang bắt đầu vượt quá tổng số lượng
            return Page.empty(pageable);
        }

        List<BestSellingBookDto> pageContent = allWorstSellingBooks.subList(start, end);

        return new PageImpl<>(pageContent, pageable, totalBooks);
    }

    // --- PHƯƠNG THỨC MỚI: SÁCH SẮP HẾT HÀNG CÓ PHÂN TRANG ---
    @Override
    public Page<BestSellingBookDto> getLowStockAlertsPaginated(int threshold, int page, int size) {
        // 1. GỌI BOOK SERVICE LẤY TẤT CẢ SÁCH VÀ TỒN KHO
        String url = bookServiceBaseUrl + "/api/book/all-details";
        BookDetailDto[] allBooksArray;
        try {
            allBooksArray = restTemplate.exchange(
                    url, HttpMethod.GET, null, BookDetailDto[].class
            ).getBody();
        } catch (Exception e) {
            System.err.println("Lỗi khi gọi Book Service để lấy chi tiết sách: " + e.getMessage());
            return Page.empty();
        }

        if (allBooksArray == null) {
            return Page.empty();
        }

        // 2. LỌC VÀ CHUYỂN ĐỔI: Sách có tồn kho dưới ngưỡng (threshold)
        List<BestSellingBookDto> lowStockBooks = Arrays.stream(allBooksArray)
                .filter(bookDetail -> bookDetail.getStockQuantity() < threshold)
                .map(bookDetail -> {
                    BestSellingBookDto dto = new BestSellingBookDto(
                            bookDetail.getId(),
                            bookDetail.getBookName(),
                            0
                    );
                    dto.setStockQuantity(bookDetail.getStockQuantity());
                    dto.setStockStatus("Sắp hết hàng (" + bookDetail.getStockQuantity() + ")");
                    return dto;
                })
                // Sắp xếp theo số lượng tồn kho TĂNG DẦN (sách ít tồn kho nhất lên đầu)
                .sorted(Comparator.comparing(BestSellingBookDto::getStockQuantity))
                .collect(Collectors.toList());

        // 3. PHÂN TRANG THỦ CÔNG
        int totalBooks = lowStockBooks.size();
        Pageable pageable = PageRequest.of(page, size);

        int startIdx = (int) pageable.getOffset();
        int endIdx = Math.min((startIdx + size), totalBooks);

        if (startIdx > totalBooks) {
            return Page.empty(pageable);
        }

        List<BestSellingBookDto> pageContent = lowStockBooks.subList(startIdx, endIdx);

        return new PageImpl<>(pageContent, pageable, totalBooks);
    }


    // --- PHƯƠNG THỨC MỚI: SÁCH BÁN CHẠY ĐỀU CÓ PHÂN TRANG ---
    @Override
    public Page<BestSellingBookDto> getConsistentSellersPaginated(int months, int minAvgMonthlySales, int page, int size) {
        // 1. TÍNH KHOẢNG THỜI GIAN
        LocalDate endDate = LocalDate.now();
        // Lấy từ đầu tháng của 'months' tháng trước
        LocalDate startDate = endDate.minusMonths(months).withDayOfMonth(1);
        Date start = Date.from(startDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date end = Date.from(endDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

        // 2. AGGREGATION: TÍNH TỔNG SỐ LƯỢNG BÁN TRONG KHOẢNG THỜI GIAN
        Criteria dateCriteria = Criteria.where("dateOrder").gte(start).lte(end);
        Criteria statusCriteria = Criteria.where("shippingStatus").nin("Chờ xử lý", "Đã hủy");
        MatchOperation matchOperation = Aggregation.match(new Criteria().andOperator(dateCriteria, statusCriteria));

        Aggregation aggregation = Aggregation.newAggregation(
                matchOperation,
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

        // 3. LỌC VÀ SẮP XẾP: Bán chạy đều (totalSold / months >= minAvgMonthlySales)
        List<BestSellingBookDto> consistentSellers = results.getMappedResults().stream()
                .filter(dto -> {
                    // Tính trung bình bán hàng theo tháng
                    double averageMonthlySales = (double) dto.getTotalSold() / months;
                    return averageMonthlySales >= minAvgMonthlySales;
                })
                // Sắp xếp theo số lượng bán được GIẢM DẦN
                .sorted(Comparator.comparing(BestSellingBookDto::getTotalSold).reversed())
                .collect(Collectors.toList());

        // 4. PHÂN TRANG THỦ CÔNG
        int totalBooks = consistentSellers.size();
        Pageable pageable = PageRequest.of(page, size);

        int startIdx = (int) pageable.getOffset();
        int endIdx = Math.min((startIdx + size), totalBooks);

        if (startIdx > totalBooks) {
            return Page.empty(pageable);
        }

        List<BestSellingBookDto> pageContent = consistentSellers.subList(startIdx, endIdx);

        return new PageImpl<>(pageContent, pageable, totalBooks);
    }

    @Override
    public Optional<Order> assignDeliveryUnit(String orderId, String deliveryUnitId) {
        return orderRepository.findById(orderId)
                .map(order -> {
                    // Cập nhật DeliveryUnitId
                    order.setDeliveryUnitId(deliveryUnitId);
                    return orderRepository.save(order);
                });
    }

    @Override
    public Optional<Order> assignShipper(String orderId, String shipperId) {
        return orderRepository.findById(orderId).map(order -> {
            order.setShipperId(shipperId);
            return orderRepository.save(order);
        });
    }

    @Override
    public Page<Order> getOrdersByDeliveryUnitId(String deliveryUnitId, int page, int size) {
        // Yêu cầu: sắp xếp theo ngày đặt hàng giảm dần
        Pageable pageable = PageRequest.of(page, size, Sort.by("dateOrder").descending());

        // Sử dụng phương thức findByDeliveryUnitId từ OrderRepository
        return orderRepository.findByDeliveryUnitId(deliveryUnitId, pageable);
    }

    @Override
    public Page<Order> getOrdersByShipperId(String shipperId, int page, int size) {
        // Sắp xếp theo ngày đặt hàng giảm dần
        Pageable pageable = PageRequest.of(page, size, Sort.by("dateOrder").descending());

        // Gọi repository
        return orderRepository.findByShipperId(shipperId, pageable);
    }

    @Override
    public String getFullAddressByOrderId(String orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng với ID: " + orderId));

        // Ghép địa chỉ theo format mong muốn
        StringBuilder addressBuilder = new StringBuilder();
        if (order.getNote() != null && !order.getNote().isBlank()) {
            addressBuilder.append(order.getNote()).append(", ");
        }
        if (order.getWard() != null && !order.getWard().isBlank()) {
            addressBuilder.append(order.getWard()).append(", ");
        }
        if (order.getDistrict() != null && !order.getDistrict().isBlank()) {
            addressBuilder.append(order.getDistrict()).append(", ");
        }
        if (order.getCity() != null && !order.getCity().isBlank()) {
            addressBuilder.append(order.getCity()).append(", ");
        }
        if (order.getCountry() != null && !order.getCountry().isBlank()) {
            addressBuilder.append(order.getCountry());
        }

        return addressBuilder.toString().replaceAll(", $", ""); // bỏ dấu , thừa
    }

}