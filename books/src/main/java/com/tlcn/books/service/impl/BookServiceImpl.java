package com.tlcn.books.service.impl;

import com.tlcn.books.dto.BookDto;
import com.tlcn.books.dto.BookFilterInputDto;
import com.tlcn.books.dto.BookWithDiscountDto;
import com.tlcn.books.entity.Book;
import com.tlcn.books.entity.BookDiscount;
import com.tlcn.books.entity.Discount;
import com.tlcn.books.exception.BookAlreadyExistsException;
import com.tlcn.books.exception.ResourceNotFoundException;
import com.tlcn.books.mapper.BookMapper;
import com.tlcn.books.repository.BookDiscountRepository;
import com.tlcn.books.repository.BookRepository;
import com.tlcn.books.repository.DiscountRepository;
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
        existingBook.setBookName(bookDto.getBookName());
        existingBook.setBookAuthor(bookDto.getBookAuthor());
        existingBook.setBookImages(bookDto.getBookImages()); // sửa lại ở đây
        existingBook.setBookPrice(bookDto.getBookPrice());
        existingBook.setBookCategory(bookDto.getBookCategory());
        existingBook.setBookYearOfProduction(bookDto.getBookYearOfProduction());
        existingBook.setBookPublisher(bookDto.getBookPublisher());
        existingBook.setBookLanguage(bookDto.getBookLanguage());
        existingBook.setBookStockQuantity(bookDto.getBookStockQuantity());
        existingBook.setBookSupplier(bookDto.getBookSupplier());
        existingBook.setBookDescription(bookDto.getBookDescription());

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

        if(input.getBookCategory() == null || input.getBookCategory().isEmpty()){
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

}
