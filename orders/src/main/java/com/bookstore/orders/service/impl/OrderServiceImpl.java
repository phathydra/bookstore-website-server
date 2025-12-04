package com.bookstore.orders.service.impl;

import com.bookstore.orders.dto.OrderDto;
import com.bookstore.orders.dto.OrderStatusDto;
import com.bookstore.orders.dto.RevenueByMonthDto;
import com.bookstore.orders.entity.MonthlyPoints;
import com.bookstore.orders.entity.Order;
import com.bookstore.orders.entity.OrderItem;
import com.bookstore.orders.entity.Rank;
import com.bookstore.orders.mapper.OrderMapper;
import com.bookstore.orders.repository.MonthlyPointsRepository;
import com.bookstore.orders.repository.OrderRepository;
import com.bookstore.orders.repository.RankRepository;
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
    private RankRepository rankRepository;

    @Autowired
    private MonthlyPointsRepository monthlyPointsRepository;

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

    public static class BookDetailDto {
        private String id;
        private String bookName;
        private String categoryName;

        @JsonProperty("bookStockQuantity")
        private int stockQuantity;

        public BookDetailDto() {}

        public BookDetailDto(String id, String bookName) {
            this.id = id;
            this.bookName = bookName;
        }

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


    // ----------------------------------------------------------------
    // --- BẮT ĐẦU PHẦN SỬA LỖI `cannot find symbol` ---
    // ----------------------------------------------------------------
    @Override
    @Transactional
    public Order createOrder(OrderDto orderDto) {

        // Chuyển DTO thành Entity, nhưng *chưa* lưu vào DB
        Order order = orderMapper.toEntity(orderDto);
        String accountId = order.getAccountId();

        // Lưu các sách đã trừ kho (để rollback nếu có lỗi)
        List<String> processedBookIds = new ArrayList<>();

        try {
            // STEP 1: XỬ LÝ KHO (DECREASE STOCK) VÀ XÓA GIỎ HÀNG (CLEAR CART)
            // Lặp qua tất cả các mặt hàng
            for (OrderItem item : order.getOrderItems()) {
                String bookId = item.getBookId();
                int quantity = item.getQuantity();

                // 1.1 Gọi API trừ kho của Book-Service
                String decreaseUrl = buildBookServiceUrl(bookId, "/decrease-stock");
                restTemplate.exchange(
                        decreaseUrl,
                        HttpMethod.PUT,
                        new HttpEntity<>(new DecreaseStockRequest(quantity), buildJsonHeaders()),
                        Void.class
                );

                // 1.2 Nếu trừ kho thành công, thêm vào danh sách đã xử lý
                processedBookIds.add(bookId);

                // 1.3 Gọi API xóa 1 item khỏi giỏ hàng
                // (Đây là phần sửa lại, dùng `removeItem` thay vì `removeItems`)
                iCartService.removeItem(accountId, bookId);
            }


            // STEP 2: LƯU ĐƠN HÀNG (SAVE ORDER)
            // Chỉ chạy khi KHO và GIỎ HÀNG đều đã xử lý thành công
            return orderRepository.save(order);

        } catch (Exception e) {
            // STEP 3: HOÀN TÁC KHO (COMPENSATING TRANSACTION)
            // Nếu có bất kỳ lỗi nào xảy ra (hết hàng, service sập, lỗi giỏ hàng...)
            // Chúng ta cần cộng lại số lượng kho cho những sách đã lỡ trừ
            System.err.println("Xảy ra lỗi khi tạo đơn hàng. Bắt đầu hoàn tác kho...");
            compensateStock(processedBookIds, order.getOrderItems());

            // Ném lỗi ra ngoài để báo cho client
            throw new RuntimeException("Không thể tạo đơn hàng, đã hoàn tác kho: " + e.getMessage(), e);
        }
    }

    /**
     * Helper: Thực hiện giao dịch bù (cộng lại kho) khi có lỗi
     * (Yêu cầu Book-Service phải có endpoint /increase-stock)
     */
    private void compensateStock(List<String> processedBookIds, List<OrderItem> allItems) {
        // Tạo một map để lấy số lượng cần hoàn tác
        Map<String, Integer> quantityMap = allItems.stream()
                .collect(Collectors.toMap(OrderItem::getBookId, OrderItem::getQuantity));

        for (String bookId : processedBookIds) {
            try {
                String increaseUrl = buildBookServiceUrl(bookId, "/increase-stock");
                int quantity = quantityMap.getOrDefault(bookId, 0);

                if (quantity > 0) {
                    restTemplate.exchange(
                            increaseUrl,
                            HttpMethod.PUT,
                            new HttpEntity<>(new DecreaseStockRequest(quantity), buildJsonHeaders()),
                            Void.class
                    );
                }
            } catch (Exception compEx) {
                // Lỗi nghiêm trọng: Không thể hoàn tác kho. Cần admin can thiệp thủ công.
                System.err.println("!!! LỖI NGHIÊM TRỌNG: KHÔNG THỂ HOÀN TÁC KHO cho bookId " + bookId + ". Cần can thiệp thủ công!!! Lỗi: " + compEx.getMessage());
            }
        }
    }

    /**
     * Helper: Xây dựng URL gọi đến Book-Service
     */
    private String buildBookServiceUrl(String bookId, String path) {
        return UriComponentsBuilder.fromHttpUrl(bookServiceBaseUrl)
                .path("/api/book/")
                .path(bookId)
                .path(path)
                .build()
                .toUriString();
    }

    /**
     * Helper: Tạo JSON headers
     */
    private HttpHeaders buildJsonHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }
    // ----------------------------------------------------------------
    // --- KẾT THÚC PHẦN SỬA LỖI ---
    // ----------------------------------------------------------------


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
    @Transactional
    public Optional<Order> updateShippingStatus(String orderId, String shippingStatus) {
        Optional<Order> orderOptional = orderRepository.findById(orderId);

        if (orderOptional.isPresent()) {
            Order order = orderOptional.get();
            order.setShippingStatus(shippingStatus);
            orderRepository.save(order);
            if(shippingStatus.equals("Đã nhận hàng")){
                updatePoints(order);
            }
            return Optional.of(order);
        }

        return Optional.empty();
    }

    private void updatePoints(Order order){
        Optional<Rank> optionalRank = rankRepository.findByAccountId(order.getAccountId());
        Double points = Math.round((order.getTotalPrice() / 1000.0) * 100.0) / 100.0;
        if(optionalRank.isPresent()){
            Double currentPoints = optionalRank.get().getPoints() + points;
            optionalRank.get().setPoints(currentPoints);
            rankRepository.save(optionalRank.get());
            updateMonthlyPoints( optionalRank.get().getAccountId(), points);
        }
    }

    private void updateMonthlyPoints(String accountId, Double points){
        LocalDate currDate = LocalDate.now();
        int month = currDate.getMonthValue();
        int year = currDate.getYear();
        Optional<MonthlyPoints> currMonthlyPoints = monthlyPointsRepository.findByAccountIdAndMonthAndYear(accountId, month, year);
        if(currMonthlyPoints.isPresent()){
            Double currPoints = currMonthlyPoints.get().getPoint() + points;
            currMonthlyPoints.get().setPoint(currPoints);
            monthlyPointsRepository.save(currMonthlyPoints.get());
        }
        else{
            MonthlyPoints monthlyPoints = new MonthlyPoints();
            monthlyPoints.setAccountId(accountId);
            monthlyPoints.setYear(year);
            monthlyPoints.setMonth(month);
            monthlyPoints.setPoint(points);
            monthlyPointsRepository.save(monthlyPoints);
        }
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
        Criteria dateCriteria = new Criteria();
        if (startDate != null && endDate != null) {
            dateCriteria.and("dateOrder").gte(startDate).lte(endDate);
        } else if (startDate != null) {
            dateCriteria.and("dateOrder").gte(startDate);
        } else if (endDate != null) {
            dateCriteria.and("dateOrder").lte(endDate);
        }

        MatchOperation matchOperation = Aggregation.match(dateCriteria);

        GroupOperation groupOperation = Aggregation.group("shippingStatus")
                .count().as("count");

        ProjectionOperation projectionOperation = Aggregation.project()
                .and("_id").as("status")
                .and("count").as("count");

        Aggregation aggregation = Aggregation.newAggregation(
                matchOperation,
                groupOperation,
                projectionOperation
        );

        AggregationResults<OrderStatusDto> results = mongoTemplate.aggregate(
                aggregation,
                "orders",
                OrderStatusDto.class
        );

        return results.getMappedResults();
    }

    @Override
    public long getTotalOrderCount(Date startDate, Date endDate) {
        Query query = new Query();
        Criteria criteria = new Criteria();
        if (startDate != null && endDate != null) {
            criteria.and("dateOrder").gte(startDate).lte(endDate);
        } else if (startDate != null) {
            criteria.and("dateOrder").gte(startDate);
        } else if (endDate != null) {
            criteria.and("dateOrder").lte(endDate);
        }
        query.addCriteria(criteria);
        return mongoTemplate.count(query, Order.class);
    }

    @Override
    public double getTotalRevenue(Date startDate, Date endDate) {
        Criteria dateCriteria = new Criteria();
        if (startDate != null && endDate != null) {
            dateCriteria.and("dateOrder").gte(startDate).lte(endDate);
        } else if (startDate != null) {
            dateCriteria.and("dateOrder").gte(startDate);
        } else if (endDate != null) {
            dateCriteria.and("dateOrder").lte(endDate);
        }
        MatchOperation matchOperation = Aggregation.match(dateCriteria);

        GroupOperation groupOperation = Aggregation.group()
                .sum("totalPrice").as("totalRevenue");

        Aggregation aggregation = Aggregation.newAggregation(
                matchOperation,
                groupOperation
        );

        AggregationResults<Document> results = mongoTemplate.aggregate(
                aggregation,
                "orders",
                Document.class
        );

        Document resultDocument = results.getUniqueMappedResult();
        if (resultDocument != null && resultDocument.containsKey("totalRevenue")) {
            return resultDocument.getDouble("totalRevenue");
        }
        return 0.0;
    }

    @Override
    public List<RevenueByMonthDto> getRevenueByMonth(Date startDate, Date endDate) {
        LocalDate startLocalDate;
        LocalDate endLocalDate;

        if (startDate != null && endDate != null) {
            startLocalDate = startDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            endLocalDate = endDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        } else {
            startLocalDate = LocalDate.now().withDayOfYear(1);
            endLocalDate = LocalDate.now();
        }

        List<RevenueByMonthDto> fullMonthList = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");

        LocalDate current = startLocalDate.withDayOfMonth(1);
        while (!current.isAfter(endLocalDate)) {
            fullMonthList.add(new RevenueByMonthDto(current.format(formatter), 0.0));
            current = current.plusMonths(1);
        }

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

        fullMonthList.forEach(monthDto -> {
            if (revenueMap.containsKey(monthDto.getPeriod())) {
                monthDto.setTotalRevenue(revenueMap.get(monthDto.getPeriod()));
            }
        });

        return fullMonthList;
    }

    @Override
    public long getUniqueCustomerCount(Date startDate, Date endDate) {
        Criteria dateCriteria = new Criteria();
        if (startDate != null && endDate != null) {
            dateCriteria.and("dateOrder").gte(startDate).lte(endDate);
        } else if (startDate != null) {
            dateCriteria.and("dateOrder").gte(startDate);
        } else if (endDate != null) {
            dateCriteria.and("dateOrder").lte(endDate);
        }
        MatchOperation matchOperation = Aggregation.match(dateCriteria);

        GroupOperation uniqueIdGroup = Aggregation.group("accountId");
        GroupOperation countGroup = Aggregation.group().count().as("uniqueCustomerCount");

        Aggregation aggregation = Aggregation.newAggregation(
                matchOperation,
                uniqueIdGroup,
                countGroup
        );

        AggregationResults<Document> results = mongoTemplate.aggregate(
                aggregation,
                "orders",
                Document.class
        );

        Document result = results.getUniqueMappedResult();
        if (Objects.nonNull(result) && Objects.nonNull(result.get("uniqueCustomerCount"))) {
            return ((Number) result.get("uniqueCustomerCount")).longValue();
        }
        return 0;
    }

    @Override
    public List<BestSellingBookDto> getTop5BestSellingCategories(Date startDate, Date endDate) {
        // 1. Xây dựng Criteria lọc
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

        // 2. Aggregation lấy tổng số lượng bán của MỖI sách
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

        // 3. Lấy danh sách bookId
        List<String> bookIds = salesByBook.stream()
                .map(doc -> doc.getString("bookId"))
                .collect(Collectors.toList());

        // 4. Gọi Book Service lấy chi tiết sách
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

        // 5. SỬA LỖI 500: Lọc ra category null TRƯỚC KHI tạo Map
        Map<String, String> bookIdToCategoryMap = Arrays.stream(bookDetailsArray)
                .filter(book -> book.getCategoryName() != null && !book.getCategoryName().isEmpty())
                .collect(Collectors.toMap(
                        BookDetailDto::getId,
                        BookDetailDto::getCategoryName,
                        (existingValue, newValue) -> existingValue
                ));


        // 6. Nhóm theo Category và tính tổng
        Map<String, Long> salesByCategory = new HashMap<>();
        for (Document doc : salesByBook) {
            String bookId = doc.getString("bookId");
            Long totalSold = ((Number) doc.get("totalSold")).longValue();

            String categoryName = bookIdToCategoryMap.get(bookId);

            if (categoryName != null) {
                salesByCategory.merge(categoryName, totalSold, Long::sum);
            }
        }

        // 7. Sắp xếp và lấy Top 5
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
        // 1. Lấy dữ liệu bán hàng từ Mongo
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

        // 2. Lấy TẤT CẢ sách từ Book Service
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

        // 3. Kết hợp dữ liệu, lọc (bán < 10) và sắp xếp
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
                .filter(dto -> dto.getTotalSold() < 10)
                .sorted(Comparator.comparing(BestSellingBookDto::getTotalSold))
                .collect(Collectors.toList());

        // 4. Phân trang thủ công
        int totalBooks = allWorstSellingBooks.size();
        Pageable pageable = PageRequest.of(page, size);

        int start = (int) pageable.getOffset();
        int end = Math.min((start + size), totalBooks);

        if (start > totalBooks) {
            return Page.empty(pageable);
        }

        List<BestSellingBookDto> pageContent = allWorstSellingBooks.subList(start, end);

        return new PageImpl<>(pageContent, pageable, totalBooks);
    }

    @Override
    public Page<BestSellingBookDto> getLowStockAlertsPaginated(int threshold, int page, int size) {
        // 1. Lấy TẤT CẢ sách từ Book Service
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

        // 2. Lọc (tồn kho < ngưỡng) và sắp xếp
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
                .sorted(Comparator.comparing(BestSellingBookDto::getStockQuantity))
                .collect(Collectors.toList());

        // 3. Phân trang thủ công
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


    @Override
    public Page<BestSellingBookDto> getConsistentSellersPaginated(int months, int minAvgMonthlySales, int page, int size) {
        // 1. Tính khoảng thời gian
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusMonths(months).withDayOfMonth(1);
        Date start = Date.from(startDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date end = Date.from(endDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

        // 2. Aggregation: Lấy tổng số lượng bán
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

        // 3. Lọc (doanh số trung bình >= ngưỡng) và sắp xếp
        List<BestSellingBookDto> consistentSellers = results.getMappedResults().stream()
                .filter(dto -> {
                    double averageMonthlySales = (double) dto.getTotalSold() / months;
                    return averageMonthlySales >= minAvgMonthlySales;
                })
                .sorted(Comparator.comparing(BestSellingBookDto::getTotalSold).reversed())
                .collect(Collectors.toList());

        // 4. Phân trang thủ công
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
        Pageable pageable = PageRequest.of(page, size, Sort.by("dateOrder").descending());
        return orderRepository.findByDeliveryUnitId(deliveryUnitId, pageable);
    }

    @Override
    public Page<Order> getOrdersByShipperId(String shipperId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("dateOrder").descending());
        return orderRepository.findByShipperId(shipperId, pageable);
    }

    @Override
    public String getFullAddressByOrderId(String orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng với ID: " + orderId));

        StringJoiner addressJoiner = new StringJoiner(", ");

        if (order.getNote() != null && !order.getNote().isBlank()) {
            addressJoiner.add(order.getNote().trim());
        }
        if (order.getWard() != null && !order.getWard().isBlank()) {
            addressJoiner.add(order.getWard().trim());
        }
        if (order.getDistrict() != null && !order.getDistrict().isBlank()) {
            addressJoiner.add(order.getDistrict().trim());
        }
        if (order.getCity() != null && !order.getCity().isBlank()) {
            addressJoiner.add(order.getCity().trim());
        }
        if (order.getCountry() != null && !order.getCountry().isBlank()) {
            addressJoiner.add(order.getCountry().trim());
        }

        return addressJoiner.toString();
    }

}