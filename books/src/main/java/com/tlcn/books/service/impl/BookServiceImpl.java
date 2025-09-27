package com.tlcn.books.service.impl;

import com.tlcn.books.dto.*;
import com.tlcn.books.entity.Book;
import com.tlcn.books.entity.BookDiscount;
import com.tlcn.books.entity.Discount;
import com.tlcn.books.entity.Import;
import com.tlcn.books.exception.BookAlreadyExistsException;
import com.tlcn.books.exception.ResourceNotFoundException;
import com.tlcn.books.mapper.BookMapper;
import com.tlcn.books.repository.BookDiscountRepository;
import com.tlcn.books.repository.BookRepository;
import com.tlcn.books.repository.DiscountRepository;
import com.tlcn.books.repository.ImportRepository;
import com.tlcn.books.service.IBookService;
import lombok.AllArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


@Service
@AllArgsConstructor
public class BookServiceImpl implements IBookService {

    private final BookRepository bookRepository;
    private final BookDiscountRepository bookDiscountRepository;
    private final DiscountRepository discountRepository;
    private final ImportRepository importRepository;

    private static final Map<String, List<String>> MAIN_CATEGORIES = new HashMap<>();

    static {
        MAIN_CATEGORIES.put("Văn Học", List.of("Tiểu thuyết", "Truyện ngắn", "Thơ ca", "Kịch", "Ngụ ngôn"));
        MAIN_CATEGORIES.put("Giáo Dục & Học Thuật", List.of("Sách giáo khoa", "Sách tham khảo", "Ngoại ngữ", "Sách khoa học"));
        MAIN_CATEGORIES.put("Kinh Doanh & Phát Triển Bản Thân", List.of("Quản trị", "Tài chính", "Khởi nghiệp", "Lãnh đạo", "Kỹ năng sống"));
        MAIN_CATEGORIES.put("Khoa Học & Công Nghệ", List.of("Vật lý", "Hóa học", "Sinh học", "Công nghệ", "Lập trình"));
        MAIN_CATEGORIES.put("Lịch Sử & Địa Lý", List.of("Lịch sử thế giới", "Lịch sử Việt Nam", "Địa lý"));
        MAIN_CATEGORIES.put("Tôn Giáo & Triết Học", List.of("Phật giáo", "Thiên Chúa giáo", "Hồi giáo", "Triết học"));
        MAIN_CATEGORIES.put("Sách Thiếu Nhi", List.of("Truyện cổ tích", "Truyện tranh", "Sách giáo dục trẻ em"));
        MAIN_CATEGORIES.put("Văn Hóa & Xã Hội", List.of("Du lịch", "Nghệ thuật", "Tâm lý - xã hội"));
        MAIN_CATEGORIES.put("Sức Khỏe & Ẩm Thực", List.of("Nấu ăn", "Dinh dưỡng", "Thể dục - thể thao"));
    }

    @Override
    public void createBook(BookDto bookDto) {
        Book book = BookMapper.mapToBook(bookDto, new Book());

        if (bookDto.getBookId() != null && bookRepository.existsById(bookDto.getBookId())) {
            throw new BookAlreadyExistsException("Trùng ID: " + bookDto.getBookId());
        }


        if (book.getBookStockQuantity() == 0) {
            book.setBookStockQuantity(1); // Set số lượng mặc định là 1
        }

        Book savedBook = bookRepository.save(book);
        System.out.println("Saved Book ID: " + savedBook.getBookId());
    }
    @Override
    public void updateBook(String bookId, BookDto bookDto) {
        Optional<Book> optionalBook = bookRepository.findById(bookId);
        if (optionalBook.isEmpty()) {
            throw new ResourceNotFoundException("Không tìm thấy sách với ID: " + bookId);
        }

        Book existingBook = optionalBook.get();

        // Cập nhật từng trường nếu giá trị mới hợp lệ
        // Đối với các trường String
        if (bookDto.getBookName() != null && !bookDto.getBookName().trim().isEmpty()) {
            existingBook.setBookName(bookDto.getBookName());
        }
        if (bookDto.getBookAuthor() != null && !bookDto.getBookAuthor().trim().isEmpty()) {
            existingBook.setBookAuthor(bookDto.getBookAuthor());
        }
        if (bookDto.getBookImages() != null && !bookDto.getBookImages().isEmpty()) {
            existingBook.setBookImages(bookDto.getBookImages());
        }
        if (bookDto.getBookCategory() != null && !bookDto.getBookCategory().trim().isEmpty()) {
            existingBook.setBookCategory(bookDto.getBookCategory());
        }
        if (bookDto.getBookPublisher() != null && !bookDto.getBookPublisher().trim().isEmpty()) {
            existingBook.setBookPublisher(bookDto.getBookPublisher());
        }
        if (bookDto.getBookLanguage() != null && !bookDto.getBookLanguage().trim().isEmpty()) {
            existingBook.setBookLanguage(bookDto.getBookLanguage());
        }
        if (bookDto.getBookSupplier() != null && !bookDto.getBookSupplier().trim().isEmpty()) {
            existingBook.setBookSupplier(bookDto.getBookSupplier());
        }
        if (bookDto.getBookDescription() != null && !bookDto.getBookDescription().trim().isEmpty()) {
            existingBook.setBookDescription(bookDto.getBookDescription());
        }

        // Đối với các trường kiểu nguyên thủy (double, int)
        // Cập nhật chỉ khi giá trị không phải là giá trị mặc định (0) và hợp lệ
        if (bookDto.getBookPrice() != 0) {
            existingBook.setBookPrice(bookDto.getBookPrice());
        }
        if (bookDto.getBookYearOfProduction() != 0) {
            existingBook.setBookYearOfProduction(bookDto.getBookYearOfProduction());
        }
        if (bookDto.getBookStockQuantity() != 0) {
            existingBook.setBookStockQuantity(bookDto.getBookStockQuantity());
        }
        if (bookDto.getImportPrice() != null) {
            existingBook.setImportPrice(bookDto.getImportPrice());
        }

        bookRepository.save(existingBook);
    }


    @Override
    public void deleteBook(String bookId) {
        Optional<Book> optionalBook = bookRepository.findById(bookId);
        if (optionalBook.isEmpty()) {
            throw new ResourceNotFoundException("Không tìm thấy sách với ID: " + bookId);
        }

        bookRepository.deleteById(bookId);
        System.out.println("Đã xóa sách với ID: " + bookId);
    }

    @Override
    public Page<BookWithDiscountDto> getAllBooks(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Book> books = bookRepository.findAllBy(pageable);
        Page<BookWithDiscountDto> bookWithDiscountDto =
                books.map(book -> {
                    BookWithDiscountDto bookWithDiscount =
                            BookMapper.mapToBookWithDiscountDto(book, new BookWithDiscountDto());
                    Optional<BookDiscount> bookDiscount =
                            bookDiscountRepository.findByBookId(bookWithDiscount.getBookId());
                    if(bookDiscount.isPresent()){
                        Optional<Discount> discount = discountRepository.findById(bookDiscount.get().getDiscountId());
                        bookWithDiscount.setPercentage(discount.get().getPercentage());
                        double discountedPrice = Math.ceil(bookWithDiscount.getBookPrice() * (1 - bookWithDiscount.getPercentage() / (double)100));
                        bookWithDiscount.setDiscountedPrice(discountedPrice);
                    }
                    return bookWithDiscount;
                });
        return bookWithDiscountDto;
    }

    @Override
    public BookWithDiscountDto getBookById(String bookId) {
        Optional<Book> optionalBook = bookRepository.findById(bookId);
        BookWithDiscountDto bookWithDiscount = new BookWithDiscountDto();
        if(optionalBook.isPresent()) {
            bookWithDiscount =
                    BookMapper.mapToBookWithDiscountDto(optionalBook.get(), new BookWithDiscountDto());
            Optional<BookDiscount> bookDiscount =
                    bookDiscountRepository.findByBookId(bookWithDiscount.getBookId());
            if (bookDiscount.isPresent()) {
                Optional<Discount> discount = discountRepository.findById(bookDiscount.get().getDiscountId());
                bookWithDiscount.setPercentage(discount.get().getPercentage());
                double discountedPrice = Math.ceil(bookWithDiscount.getBookPrice() * (1 - bookWithDiscount.getPercentage() / (double) 100));
                bookWithDiscount.setDiscountedPrice(discountedPrice);
            }
        } else{
            throw new RuntimeException("Không tìm thấy sách với ID: " + bookId);
        }
        return bookWithDiscount;
    }

    @Override
    public Page<BookWithDiscountDto> searchBooks(int page, int size, String input){
        Pageable pageable = PageRequest.of(page, size);
        String[] terms = input.trim().split("\\s+");
        String regex = "(?i)(" + String.join("|", terms) + ")";
        Page<Book> books = bookRepository.findBySearchTerms(regex, pageable);
        Page<BookWithDiscountDto> bookWithDiscountDto =
                books.map(book -> {
                    BookWithDiscountDto bookWithDiscount =
                            BookMapper.mapToBookWithDiscountDto(book, new BookWithDiscountDto());
                    Optional<BookDiscount> bookDiscount =
                            bookDiscountRepository.findByBookId(bookWithDiscount.getBookId());
                    if(bookDiscount.isPresent()){
                        Optional<Discount> discount = discountRepository.findById(bookDiscount.get().getDiscountId());
                        bookWithDiscount.setPercentage(discount.get().getPercentage());
                        double discountedPrice = Math.ceil(bookWithDiscount.getBookPrice() * (1 - bookWithDiscount.getPercentage() / (double)100));
                        bookWithDiscount.setDiscountedPrice(discountedPrice);
                    }
                    return bookWithDiscount;
                });
        return bookWithDiscountDto;
    }

    @Override
    public List<BookDto> getRecommendedBooks(String bookId) {
        Optional<Book> optionalBook = bookRepository.findById(bookId);

        List<Book> recommendedBooks = new ArrayList<>();

        if (optionalBook.isPresent()) {
            Book book = optionalBook.get();

            List<Book> booksByCategory = bookRepository.findTop5ByMainCategoryAndBookIdNot(
                    book.getMainCategory(), bookId
            );

            List<Book> booksByAuthor = bookRepository.findTop5ByBookAuthorAndBookIdNot(
                    book.getBookAuthor(), bookId
            );

            // Thêm sách theo thể loại
            for (Book b : booksByCategory) {
                if (!recommendedBooks.contains(b)) {
                    recommendedBooks.add(b);
                }
            }

            // Thêm sách theo tác giả
            for (Book b : booksByAuthor) {
                if (!recommendedBooks.contains(b)) {
                    recommendedBooks.add(b);
                }
            }
        }

        // Nếu không tìm thấy bookId hoặc chưa đủ số lượng gợi ý → lấy thêm sách bất kỳ
        if (recommendedBooks.size() < 5) {
            List<Book> additionalBooks = bookRepository.findTop5ByOrderByBookNameAsc();
            for (Book additionalBook : additionalBooks) {
                if (!recommendedBooks.contains(additionalBook)
                        && !additionalBook.getBookId().equals(bookId)) {
                    recommendedBooks.add(additionalBook);
                }
                if (recommendedBooks.size() >= 5) break;
            }
        }

        return recommendedBooks.stream()
                .limit(5)
                .map(b -> BookMapper.mapToBookDto(b, new BookDto()))
                .collect(Collectors.toList());
    }


    @Override
    public List<BookDto> getSearchRecommendedBooks(String bookName, List<String> excludedBooks){
        List<Book> bookList = bookRepository.findTop3ByBookNameContainingIgnoreCaseAndBookIdNotIn(bookName, excludedBooks);
        return bookList.stream().map(book -> BookMapper.mapToBookDto(book, new BookDto())).toList();
    }

    public List<BookDto> getDiscountedBooksAdmin(String discountId){
        List<BookDiscount> bookDiscounts = bookDiscountRepository.findByDiscountId(discountId);
        List<String> bookIds = bookDiscounts.stream()
                .map(BookDiscount::getBookId)
                .toList();
        List<Book> books = bookRepository.findAllById(bookIds);
        return books.stream().map(book -> BookMapper.mapToBookDto(book, new BookDto())).toList();
    }

    @Override
    public Page<BookWithDiscountDto> getAllDiscountedBooks(int page, int size){
        Pageable pageable = PageRequest.of(page, size);

        List<BookDiscount> bookDiscounts = bookDiscountRepository.findAll();
        List<Discount> discounts = discountRepository.findAll();
        List<String> availableDiscounts = new ArrayList<>();
        LocalDate currDate = LocalDate.now();

        for (Discount discount : discounts) {
            LocalDate startDate = discount.getStartDate().toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
            LocalDate endDate = discount.getEndDate().toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();

            if ((startDate.isEqual(currDate) || startDate.isBefore(currDate)) &&
                    (endDate.isEqual(currDate) || endDate.isAfter(currDate))) {
                availableDiscounts.add(discount.getId());
            }
        }

        List<String> bookIds = bookDiscounts.stream()
                .filter(bookDiscount -> availableDiscounts.contains(bookDiscount.getDiscountId()))
                .map(BookDiscount::getBookId)
                .toList();
        Page<Book> books = bookRepository.findByBookIdIn(bookIds, pageable);
        Page<BookWithDiscountDto> bookWithDiscountDto =
                books.map(book -> {
                    BookWithDiscountDto bookWithDiscount =
                            BookMapper.mapToBookWithDiscountDto(book, new BookWithDiscountDto());
                    Optional<BookDiscount> bookDiscount =
                            bookDiscountRepository.findByBookId(bookWithDiscount.getBookId());
                    if(bookDiscount.isPresent()){
                        Optional<Discount> discount = discountRepository.findById(bookDiscount.get().getDiscountId());
                        bookWithDiscount.setPercentage(discount.get().getPercentage());
                        double discountedPrice = Math.ceil(bookWithDiscount.getBookPrice() * (1 - bookWithDiscount.getPercentage() / (double)100));
                        bookWithDiscount.setDiscountedPrice(discountedPrice);
                    }
                    return bookWithDiscount;
                });
        return bookWithDiscountDto;
    }

    @Override
    public ByteArrayInputStream exportDiscountedBooks(String discountId) throws IOException {
        List<BookDiscount> bookDiscounts = bookDiscountRepository.findByDiscountId(discountId);
        List<String> bookIds = bookDiscounts.stream()
                .map(BookDiscount::getBookId)
                .toList();
        List<Book> discountedBooks = bookRepository.findAllById(bookIds);
        try(Workbook workbook = new XSSFWorkbook();
            ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Discounted books of " + discountId);

            Row headerRow = sheet.createRow(0);
            CellStyle headerCellStyle = workbook.createCellStyle();
            Font font = workbook.createFont();;
            font.setBold(true);
            headerCellStyle.setFont(font);

            String[] headers = {"Book ID", "Book Name"};
            for (int col = 0; col < headers.length; col++){
                Cell cell = headerRow.createCell(col);
                cell.setCellValue(headers[col]);
                cell.setCellStyle(headerCellStyle);
            }

            int rowIdx = 1;
            for(Book book : discountedBooks){
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(book.getBookId());
                row.createCell(1).setCellValue(book.getBookName());
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        }
    }

    @Override
    public Page<BookWithDiscountDto> getBooksByMainCategory(String mainCategory, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Book> books = bookRepository.findByMainCategory(mainCategory, pageable);
        Page<BookWithDiscountDto> bookWithDiscountDto =
                books.map(book -> {
                    BookWithDiscountDto bookWithDiscount =
                            BookMapper.mapToBookWithDiscountDto(book, new BookWithDiscountDto());
                    Optional<BookDiscount> bookDiscount =
                            bookDiscountRepository.findByBookId(bookWithDiscount.getBookId());
                    if(bookDiscount.isPresent()){
                        Optional<Discount> discount = discountRepository.findById(bookDiscount.get().getDiscountId());
                        bookWithDiscount.setPercentage(discount.get().getPercentage());
                        double discountedPrice = Math.ceil(bookWithDiscount.getBookPrice() * (1 - bookWithDiscount.getPercentage() / (double)100));
                        bookWithDiscount.setDiscountedPrice(discountedPrice);
                    }
                    return bookWithDiscount;
                });
        return bookWithDiscountDto;
    }
    @Override
    public Page<BookWithDiscountDto> getBooksByBookCategory(String bookCategory, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Book> books = bookRepository.findByBookCategory(bookCategory, pageable);
        Page<BookWithDiscountDto> bookWithDiscountDto =
                books.map(book -> {
                    BookWithDiscountDto bookWithDiscount =
                            BookMapper.mapToBookWithDiscountDto(book, new BookWithDiscountDto());
                    Optional<BookDiscount> bookDiscount =
                            bookDiscountRepository.findByBookId(bookWithDiscount.getBookId());
                    if(bookDiscount.isPresent()){
                        Optional<Discount> discount = discountRepository.findById(bookDiscount.get().getDiscountId());
                        bookWithDiscount.setPercentage(discount.get().getPercentage());
                        double discountedPrice = Math.ceil(bookWithDiscount.getBookPrice() * (1 - bookWithDiscount.getPercentage() / (double)100));
                        bookWithDiscount.setDiscountedPrice(discountedPrice);
                    }
                    return bookWithDiscount;
                });
        return bookWithDiscountDto;
    }

    @Override
    public Page<BookWithDiscountDto> filterBooks(
            BookFilterInputDto input,
            int page,
            int size) {

        Pageable pageable = PageRequest.of(page, size);
        double min = (input.getMinPrice() != null) ? input.getMinPrice() : 0;
        double max = (input.getMaxPrice() != null) ? input.getMaxPrice() : Double.MAX_VALUE;

        String authorRegex = (input.getBookAuthor() == null || input.getBookAuthor().isBlank()) ? ".*" : input.getBookAuthor();

        FilteredCategories filtered = processCategories(input.getMainCategory(), input.getBookCategory());

        List<Pattern> mainCategoryPatterns = normalizeToPatterns(filtered.main());
        List<Pattern> subCategoryPatterns  = normalizeToPatterns(filtered.sub());
        List<Pattern> publisherPatterns = normalizeToPatterns(input.getBookPublisher());
        List<Pattern> supplierPatterns = normalizeToPatterns(input.getBookSupplier());

        Page<Book> books;

        if(input.getBookCategory().isEmpty() || input.getBookCategory() == null){
            books = bookRepository.filterBooksByAllNoSubCategory(
                    authorRegex,
                    mainCategoryPatterns,
                    publisherPatterns,
                    supplierPatterns,
                    min,
                    max,
                    pageable
            );
        }
        else{
            books = bookRepository.filterBooksByAll(
                    authorRegex,
                    mainCategoryPatterns,
                    subCategoryPatterns,
                    publisherPatterns,
                    supplierPatterns,
                    min,
                    max,
                    pageable
            );
        }

        return books.map(book -> {
            BookWithDiscountDto dto = BookMapper.mapToBookWithDiscountDto(book, new BookWithDiscountDto());

            bookDiscountRepository.findByBookId(dto.getBookId()).ifPresent(bookDiscount -> {
                discountRepository.findById(bookDiscount.getDiscountId()).ifPresent(discount -> {
                    dto.setPercentage(discount.getPercentage());
                    double discountedPrice = Math.ceil(
                            dto.getBookPrice() * (1 - dto.getPercentage() / 100.0)
                    );
                    dto.setDiscountedPrice(discountedPrice);
                });
            });

            return dto;
        });
    }

    private FilteredCategories processCategories(List<String> mainCategories, List<String> subCategories) {
        if (mainCategories == null) mainCategories = new ArrayList<>();
        if (subCategories == null) subCategories = new ArrayList<>();

        Set<String> mainCopy = new HashSet<>(mainCategories);

        for (Map.Entry<String, List<String>> entry : MAIN_CATEGORIES.entrySet()) {
            String main = entry.getKey();
            List<String> subs = entry.getValue();

            // if any subcategory of this main is selected -> remove the main
            boolean subChosen = subCategories.stream().anyMatch(subs::contains);
            if (subChosen) {
                mainCopy.remove(main);
            }
        }

        return new FilteredCategories(new ArrayList<>(mainCopy), subCategories);
    }

    private record FilteredCategories(List<String> main, List<String> sub) {}

    private List<Pattern> normalizeToPatterns(List<String> values) {
        if (values == null || values.isEmpty()) {
            return List.of(Pattern.compile(".*", Pattern.CASE_INSENSITIVE));
        }
        return values.stream()
                .map(v -> Pattern.compile(v, Pattern.CASE_INSENSITIVE))
                .toList();
    }


    @Override
    @Transactional
    public void decreaseStock(String bookId, int quantity) {
        Optional<Book> optionalBook = bookRepository.findById(bookId);
        if (optionalBook.isEmpty()) {
            throw new ResourceNotFoundException("Không tìm thấy sách với ID: " + bookId);
        }

        Book book = optionalBook.get();
        int currentStock = book.getBookStockQuantity();
        int newStock = currentStock - quantity;

        if (newStock < 0) {
            throw new IllegalArgumentException("Không đủ số lượng sách cho ID: " + bookId + ". Hiện có: " + currentStock);
        }

        book.setBookStockQuantity(newStock);
        bookRepository.save(book);
    }

    @Override
    @Transactional
    public void increaseStock(String bookId, int quantity) {
        Optional<Book> optionalBook = bookRepository.findById(bookId);
        if (optionalBook.isEmpty()) {
            throw new ResourceNotFoundException("Không tìm thấy sách với ID: " + bookId);
        }

        Book book = optionalBook.get();
        int currentStock = book.getBookStockQuantity();
        int newStock = currentStock + quantity;

        book.setBookStockQuantity(newStock);
        bookRepository.save(book);
    }

    @Override
    public ByteArrayInputStream exportAllBooks() throws IOException {
        List<Book> books = bookRepository.findAll();

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("All Books");

            CellStyle headerCellStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            headerCellStyle.setFont(font);

            String[] headers = {
                    "Book ID", "Book Name", "Author", "Images", "Price", "Main Category",
                    "Category", "Year", "Publisher", "Language", "Stock Quantity", "Supplier", "Description"
            };

            Row headerRow = sheet.createRow(0);
            for (int col = 0; col < headers.length; col++) {
                Cell cell = headerRow.createCell(col);
                cell.setCellValue(headers[col]);
                cell.setCellStyle(headerCellStyle);
            }

            int rowIdx = 1;
            for (Book book : books) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(book.getBookId());
                row.createCell(1).setCellValue(book.getBookName());
                row.createCell(2).setCellValue(book.getBookAuthor());
                row.createCell(3).setCellValue(
                        book.getBookImages() != null ? String.join(";", book.getBookImages()) : ""
                );
                row.createCell(4).setCellValue(book.getBookPrice());
                row.createCell(5).setCellValue(book.getMainCategory());
                row.createCell(6).setCellValue(book.getBookCategory());
                row.createCell(7).setCellValue(book.getBookYearOfProduction());
                row.createCell(8).setCellValue(book.getBookPublisher());
                row.createCell(9).setCellValue(book.getBookLanguage());
                row.createCell(10).setCellValue(book.getBookStockQuantity());
                row.createCell(11).setCellValue(book.getBookSupplier());
                row.createCell(12).setCellValue(book.getBookDescription());
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        }
    }

    @Override
    public void importBooks(MultipartFile file) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);

            List<Book> books = new ArrayList<>();

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                Book book = new Book();
                book.setBookId(UUID.randomUUID().toString());
                book.setBookName(row.getCell(0).getStringCellValue());
                book.setBookAuthor(row.getCell(1).getStringCellValue());

                // Nhiều ảnh, phân tách bằng ;
                if (row.getCell(2) != null) {
                    String imagesStr = row.getCell(2).getStringCellValue();
                    List<String> images = Arrays.stream(imagesStr.split(";"))
                            .map(String::trim)
                            .filter(s -> !s.isEmpty())
                            .toList();
                    book.setBookImages(images);
                }

                book.setBookPrice(row.getCell(3).getNumericCellValue());
                book.setMainCategory(row.getCell(4).getStringCellValue());
                book.setBookCategory(row.getCell(5).getStringCellValue());
                book.setBookYearOfProduction((int) row.getCell(6).getNumericCellValue());
                book.setBookPublisher(row.getCell(7).getStringCellValue());
                book.setBookLanguage(row.getCell(8).getStringCellValue());
                book.setBookStockQuantity((int) row.getCell(9).getNumericCellValue());
                book.setBookSupplier(row.getCell(10).getStringCellValue());
                book.setBookDescription(row.getCell(11) != null ? row.getCell(11).getStringCellValue() : null);

                books.add(book);
            }

            bookRepository.saveAll(books);
        }
    }

    @Override
    public Page<BookWithDiscountDto> getBooksByStockQuantity(int quantity, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Book> books = bookRepository.findByBookStockQuantity(quantity, pageable);
        return books.map(this::mapBookToBookWithDiscountDto);
    }

    @Override
    public Page<BookWithDiscountDto> getBooksInStock(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Book> books = bookRepository.findByBookStockQuantityGreaterThan(0, pageable);
        return books.map(this::mapBookToBookWithDiscountDto);
    }

    // Tạo một phương thức private để tái sử dụng logic tính toán chiết khấu
    private BookWithDiscountDto mapBookToBookWithDiscountDto(Book book) {
        BookWithDiscountDto bookWithDiscount = BookMapper.mapToBookWithDiscountDto(book, new BookWithDiscountDto());
        Optional<BookDiscount> bookDiscount = bookDiscountRepository.findByBookId(bookWithDiscount.getBookId());
        if (bookDiscount.isPresent()) {
            Optional<Discount> discount = discountRepository.findById(bookDiscount.get().getDiscountId());
            discount.ifPresent(d -> {
                bookWithDiscount.setPercentage(d.getPercentage());
                double discountedPrice = Math.ceil(bookWithDiscount.getBookPrice() * (1 - d.getPercentage() / (double) 100));
                bookWithDiscount.setDiscountedPrice(discountedPrice);
            });
        }
        return bookWithDiscount;
    }
    @Override
    public ByteArrayInputStream exportBooksInStock() throws IOException {
        List<Book> books = bookRepository.findByBookStockQuantityGreaterThan(0, Pageable.unpaged()).getContent();
        return exportBooksToExcel(books);
    }

    @Override
    public ByteArrayInputStream exportBooksOutOfStock() throws IOException {
        List<Book> books = bookRepository.findByBookStockQuantity(0, Pageable.unpaged()).getContent();
        return exportBooksToExcel(books);
    }

    // tái sử dụng logic xuất Excel
    private ByteArrayInputStream exportBooksToExcel(List<Book> books) throws IOException {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Books");
            String[] headers = {
                    "Book ID", "Book Name", "Author", "Images", "Price", "Main Category",
                    "Category", "Year", "Publisher", "Language", "Stock Quantity", "Supplier", "Description"
            };

            Font font = workbook.createFont();
            font.setBold(true);
            CellStyle headerCellStyle = workbook.createCellStyle();
            headerCellStyle.setFont(font);

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerCellStyle);
            }

            int rowIdx = 1;
            for (Book book : books) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(book.getBookId());
                row.createCell(1).setCellValue(book.getBookName());
                row.createCell(2).setCellValue(book.getBookAuthor());
                row.createCell(3).setCellValue(
                        book.getBookImages() != null ? String.join(";", book.getBookImages()) : ""
                );
                row.createCell(4).setCellValue(book.getBookPrice());
                row.createCell(5).setCellValue(book.getMainCategory());
                row.createCell(6).setCellValue(book.getBookCategory());
                row.createCell(7).setCellValue(book.getBookYearOfProduction());
                row.createCell(8).setCellValue(book.getBookPublisher());
                row.createCell(9).setCellValue(book.getBookLanguage());
                row.createCell(10).setCellValue(book.getBookStockQuantity());
                row.createCell(11).setCellValue(book.getBookSupplier());
                row.createCell(12).setCellValue(book.getBookDescription());
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        }
    }

    @Override
    @Transactional
    public void importStock(List<BookDto> booksToImport) {
        for (BookDto bookDto : booksToImport) {
            Optional<Book> existingBook = bookRepository.findByBookNameIgnoreCaseAndBookAuthorIgnoreCase(bookDto.getBookName(), bookDto.getBookAuthor());

            Book book;
            if (existingBook.isPresent()) {
                // Trường hợp 1: Sách đã tồn tại
                book = existingBook.get();
                book.setBookStockQuantity(book.getBookStockQuantity() + bookDto.getBookStockQuantity());
                bookRepository.save(book);
            } else {
                // Trường hợp 2: Sách mới
                book = BookMapper.mapToBook(bookDto, new Book());
                book.setBookStockQuantity(bookDto.getBookStockQuantity());
                bookRepository.save(book);
            }

            // Tạo một document Import mới để lưu lịch sử
            Import newImport = new Import();
            newImport.setBookId(book.getBookId());
            newImport.setBookName(book.getBookName());
            newImport.setBookAuthor(book.getBookAuthor());
            newImport.setBookSupplier(book.getBookSupplier());
            newImport.setQuantity(bookDto.getBookStockQuantity());
            newImport.setImportPrice(bookDto.getImportPrice());
            newImport.setImportDate(LocalDateTime.now());

            // Lưu document Import vào collection "imports"
            importRepository.save(newImport);
        }
    }

    @Override
    public List<BookDetailForOrderDto> getBookDetailsByIds(List<String> bookIds) {
        // 1. Tìm tất cả các sách theo danh sách bookId
        List<Book> books = bookRepository.findAllById(bookIds);

        // 2. Chuyển đổi sang BookDetailForOrderDto
        return books.stream()
                .map(book -> new BookDetailForOrderDto(
                        book.getBookId(),
                        book.getBookCategory()
                ))
                .collect(Collectors.toList());
    }

    @Override
    public List<BookDetailDto> getAllBookDetails() {
        // Lấy tất cả sách từ repository
        List<Book> allBooks = bookRepository.findAll();

        // Mapping sang BookDetailDto bằng cách sử dụng stream và BookMapper
        return allBooks.stream()
                .map(book -> BookMapper.mapToBookDetailDto(book, new BookDetailDto()))
                .collect(Collectors.toList());
    }
}
