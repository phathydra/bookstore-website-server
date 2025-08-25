package com.tlcn.books.service.impl;

import com.tlcn.books.dto.BookDto;
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
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class BookServiceImpl implements IBookService {

    private final BookRepository bookRepository;
    private final BookDiscountRepository bookDiscountRepository;
    private final DiscountRepository discountRepository;

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
        if (optionalBook.isEmpty()) {
            throw new ResourceNotFoundException("Không tìm thấy sách với ID: " + bookId);
        }

        Book book = optionalBook.get();

        List<Book> booksByCategory = bookRepository.findTop5ByMainCategoryAndBookIdNot(book.getMainCategory(), bookId);

        List<Book> booksByAuthor = bookRepository.findTop5ByBookAuthorAndBookIdNot(book.getBookAuthor(), bookId);

        List<Book> recommendedBooks = new ArrayList<>();

        // Thêm sách theo thể loại và tác giả
        for (Book b : booksByCategory) {
            if (!recommendedBooks.contains(b)) {
                recommendedBooks.add(b);
            }
        }
        for (Book b : booksByAuthor) {
            if (!recommendedBooks.contains(b)) {
                recommendedBooks.add(b);
            }
        }
        if (recommendedBooks.size() < 5) {
            List<Book> additionalBooks = (List<Book>) bookRepository.findTop3ByOrderByBookNameAsc();
            for (Book additionalBook : additionalBooks) {
                if (!recommendedBooks.contains(additionalBook) && !additionalBook.getBookId().equals(bookId)) {
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
    public Page<BookWithDiscountDto> filterBooks(String bookAuthor, List<String> mainCategory, Double minPrice, Double maxPrice, List<String> bookPublisher, List<String> bookSupplier, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        double min = (minPrice != null) ? minPrice : 0;
        double max = (maxPrice != null) ? maxPrice : Double.MAX_VALUE;

        Page<Book> books;

        if (mainCategory != null && !mainCategory.isEmpty() && bookPublisher != null && !bookPublisher.isEmpty() && bookSupplier != null && !bookSupplier.isEmpty()) {
            books = bookRepository.findByBookAuthorContainingIgnoreCaseAndMainCategoryInAndBookPriceBetweenAndBookPublisherInAndBookSupplierIn(
                    bookAuthor, mainCategory, min, max, bookPublisher, bookSupplier, pageable);
        } else if (mainCategory != null && !mainCategory.isEmpty() && bookPublisher != null && !bookPublisher.isEmpty()) {
            books = bookRepository.findByBookAuthorContainingIgnoreCaseAndMainCategoryInAndBookPriceBetweenAndBookPublisherIn(
                    bookAuthor, mainCategory, min, max, bookPublisher, pageable);
        } else if (mainCategory != null && !mainCategory.isEmpty() && bookSupplier != null && !bookSupplier.isEmpty()) {
            books = bookRepository.findByBookAuthorContainingIgnoreCaseAndMainCategoryInAndBookPriceBetweenAndBookSupplierIn(
                    bookAuthor, mainCategory, min, max, bookSupplier, pageable);
        } else if (bookPublisher != null && !bookPublisher.isEmpty() && bookSupplier != null && !bookSupplier.isEmpty()) {
            books = bookRepository.findByBookAuthorContainingIgnoreCaseAndBookPriceBetweenAndBookPublisherInAndBookSupplierIn(
                    bookAuthor, min, max, bookPublisher, bookSupplier, pageable);
        } else if (mainCategory != null && !mainCategory.isEmpty()) {
            books = bookRepository.findByBookAuthorContainingIgnoreCaseAndMainCategoryInAndBookPriceBetween(
                    bookAuthor, mainCategory, min, max, pageable);
        } else if (bookPublisher != null && !bookPublisher.isEmpty()) {
            books = bookRepository.findByBookAuthorContainingIgnoreCaseAndBookPriceBetweenAndBookPublisherIn(
                    bookAuthor, min, max, bookPublisher, pageable);
        } else if (bookSupplier != null && !bookSupplier.isEmpty()) {
            books = bookRepository.findByBookAuthorContainingIgnoreCaseAndBookPriceBetweenAndBookSupplierIn(
                    bookAuthor, min, max, bookSupplier, pageable);
        } else {
            books = bookRepository.findByBookAuthorContainingIgnoreCaseAndBookPriceBetween(
                    bookAuthor, min, max, pageable);
        }

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
}
